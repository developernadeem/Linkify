package pk.edu.uaf.linkify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyService;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.GALLERY_REQUEST_CODE;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.pickFromGallery;

public class ChatActivity extends AppCompatActivity implements ServiceCallBacks {

    private static final String TAG = "ChatActivity";
    /*
     * Remote users Service Information object which has remote user IP address and port Number*/
    private NsdServiceInfo mInfo;
    private boolean isConnectionSentToService = false;
    private boolean mIsBound;

    LinkifyService mService;
    @BindView(R.id.messagesList)
    MessagesList list;
    @BindView(R.id.input)
    MessageInput input;
    private long chatId;
    private String mRemoteUserId;
    private MessagesListAdapter<LinkifyMessage> adapter;
    private ChatViewModel viewModel;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        doBindService();
        Intent intent = getIntent();
        chatId = intent.getLongExtra("id", 0);
        mRemoteUserId = intent.getStringExtra("userId");
        ChatViewModelFactory factory = new ChatViewModelFactory(ChatDataBase.getInstance(this), chatId);
        viewModel = ViewModelProviders.of(this, factory).get(ChatViewModel.class);
        adapter = new MessagesListAdapter<>(Build.SERIAL, (imageView, url, payload) -> Glide.with(ChatActivity.this).load(url).into(imageView));
        viewModel.getMessages().observe(this, linkifyMessages -> {
            viewModel.getMessages().removeObservers(ChatActivity.this);
            adapter.addToEnd(linkifyMessages, false);
        });
        if (intent.hasExtra("info")) {
            mInfo = intent.getParcelableExtra("info");
            if (mService != null) {
                isConnectionSentToService = true;
                mService.connectToSocket(mInfo, PrefUtils.getStringPref(this, USER_SERVICE_NAME));
            }
        }

        list.setAdapter(adapter);
        input.setInputListener(input -> {
            //validate and send message
            LinkifyMessage msg = new LinkifyMessage(input.toString(), new Date(), null, Build.SERIAL, chatId);
            adapter.addToStart(msg, true);
            if (mService != null)
                mService.sendMessage(input.toString());
            viewModel.insertMessage(msg);
            viewModel.updateChat(chatId,msg.getmText(),new Date());
            return true;
        });
        input.setAttachmentsListener(() -> {
            pickFromGallery(ChatActivity.this) ;
        });
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(getBaseContext(),
                LinkifyService.class), connection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

        Log.d(TAG, "doBindService: Binding.");
    }

    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            mService.setCallbacks(null);

            // Detach our existing connection.
            unbindService(connection);
            mIsBound = false;
            Log.d(TAG, "doUnbindService: Unbinding.");
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Set the service messenger and connected status
            // cast the IBinder and get MyService instance
            LinkifyService.LocalBinder binder = (LinkifyService.LocalBinder) service;

            mService = binder.getService();
            mService.setCallbacks(ChatActivity.this);
            if (!isConnectionSentToService)
                mService.connectToSocket(mInfo, PrefUtils.getStringPref(ChatActivity.this, USER_SERVICE_NAME));// register

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "Service disconnected: " + name);

            // Reset the service messenger and connection status
            ChatActivity.this.mService = null;
        }
    };

    @Override
    public void getMessageFromService(JSONObject message) {

    }

    @Override
    public void getUserMessage(String msg, int type) {
        Log.d(TAG, "getUserMessage: " + msg);
        runOnUiThread(() -> {
            LinkifyMessage message;
            if (type == 0){
                message = new LinkifyMessage(null, new Date(), msg, mRemoteUserId, chatId);
            }else {
                message = new LinkifyMessage(msg, new Date(), null, mRemoteUserId, chatId);
            }

            adapter.addToStart(message, true);
            viewModel.insertMessage(message);
            viewModel.updateChat(chatId,msg,new Date());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == GALLERY_REQUEST_CODE) {//data.getData returns the content URI for the selected Image
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                LinkifyMessage msg =new LinkifyMessage(null,new Date(),picturePath,Build.SERIAL,chatId);
                viewModel.insertMessage(msg);
                adapter.addToStart(msg,true);
                if (mService != null){
                    mService.sendImage(picturePath);
                }
            }
    }
}
