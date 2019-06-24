package pk.edu.uaf.linkify;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Fragments.VideoCallFragment;
import pk.edu.uaf.linkify.Fragments.VoiceCallFragment;
import pk.edu.uaf.linkify.Interfaces.CallFragmentEvents;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.Modal.LinkifyCalls;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyService;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.ACTION_CONNECT_INFO;
import static pk.edu.uaf.linkify.Utils.AppConstant.GALLERY_REQUEST_CODE;
import static pk.edu.uaf.linkify.Utils.AppConstant.IN_COMING_VIDEO;
import static pk.edu.uaf.linkify.Utils.AppConstant.IN_COMING_VOICE;
import static pk.edu.uaf.linkify.Utils.AppConstant.MESSAGE;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VIDEO;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VOICE;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.pickFromGallery;

public class ChatActivity extends AppCompatActivity implements ServiceCallBacks, CallFragmentEvents {
    private static final String TAG = "ChatActivity";
    /*
     * Remote users Service Information object which has remote user IP address and port Number*/
    private NsdServiceInfo mInfo;
    private boolean isConnectionSentToService = false;
    private boolean mIsBound;
    private boolean isInitiator = false;
    LinkifyService mService;
    @BindView(R.id.messagesList)
    MessagesList list;
    @BindView(R.id.input)
    MessageInput input;
    private long chatId;
    private String mRemoteUserId;
    private String mRemoteUserName;
    private String mRemoteUserAvatar;
    private MessagesListAdapter<LinkifyMessage> adapter;
    private ChatViewModel viewModel;
    private VideoCallFragment callFragment;
    private VoiceCallFragment voiceCallFragment;
    MediaPlayer mp;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setVolumeControlStream(AudioManager.STREAM_RING);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        doBindService();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent.getAction() != null) {
            mRemoteUserName = intent.getStringExtra("name");
            mRemoteUserAvatar = intent.getStringExtra("avatar");
            mRemoteUserId = intent.getStringExtra("userId");
            switch (intent.getAction()) {
                case OUT_GOING_VIDEO:

                    break;
                case OUT_GOING_VOICE:

                    break;
                case IN_COMING_VIDEO:
                    try {
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra("json"));
                        inComingVideoCall(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case IN_COMING_VOICE:
                    try {
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra("json"));
                        inComingVoiceCall(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_CONNECT_INFO:
                    initViewModel(intent);
                    connectSocket(intent);
                    break;
                case MESSAGE:
                    initViewModel(intent);
                    break;
            }
        }


    }

    private void connectSocket(Intent intent) {
        if (intent.hasExtra("info")) {
            mInfo = intent.getParcelableExtra("info");
            if (mService != null) {
                isConnectionSentToService = true;
                mService.connectToSocket(mInfo, PrefUtils.getStringPref(this, USER_SERVICE_NAME));
            }
        }
    }

    private void initViewModel(Intent intent) {
        chatId = intent.getLongExtra("id", 0);
        mRemoteUserId = intent.getStringExtra("userId");
        ChatViewModelFactory factory = new ChatViewModelFactory(ChatDataBase.getInstance(this), chatId);
        viewModel = ViewModelProviders.of(this, factory).get(ChatViewModel.class);
        adapter = new MessagesListAdapter<>(getSerial(), (imageView, url, payload) -> Glide.with(ChatActivity.this).load(url).into(imageView));
        viewModel.getMessages().observe(this, linkifyMessages -> {
            viewModel.getMessages().removeObservers(ChatActivity.this);
            adapter.addToEnd(linkifyMessages, false);
        });
        list.setAdapter(adapter);
        input.setInputListener(input -> {
            //validate and send message
            LinkifyMessage msg = new LinkifyMessage(input.toString(), new Date(), null, getSerial(), chatId);
            adapter.addToStart(msg, true);
            if (mService != null)
                mService.sendMessage(input.toString());
            viewModel.insertMessage(msg);
            viewModel.updateChat(chatId, msg.getmText(), new Date());
            return true;
        });
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(ChatActivity.this, v);
                }
            }
        });
        input.setAttachmentsListener(() -> {
            pickFromGallery(ChatActivity.this);
        });
    }

    private String getSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return "unkown";
            }
            return Build.getSerial();
        } else
            return Build.SERIAL;
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
            if (type == 0) {
                message = new LinkifyMessage(null, new Date(), msg, mRemoteUserId, chatId);
            } else {
                message = new LinkifyMessage(msg, new Date(), null, mRemoteUserId, chatId);
            }

            adapter.addToStart(message, true);
            viewModel.insertMessage(message);
            viewModel.updateChat(chatId, msg, new Date());
        });
    }

    @Override
    public void inComingVideoCall(JSONObject jsonObject) {

        callFragment = VideoCallFragment.getInstance(false,mRemoteUserName,mRemoteUserAvatar);
        replaceFragment(callFragment, "video");
        callFragment.onRemoteMessage(jsonObject);
    }

    @Override
    public void onVideoCallPicked() {

    }


    @Override
    public void inComingVoiceCall(JSONObject object) {

        voiceCallFragment = VoiceCallFragment.getInstance(false,mRemoteUserName,mRemoteUserAvatar);
        replaceFragment(voiceCallFragment, "video");
        voiceCallFragment.onRemoteMessage(object);
    }

    @Override
    public void onVideoSignals(JSONObject object) {
        if (callFragment != null) {
            callFragment.onRemoteMessage(object);
        }
    }

    @Override
    public void onVoiceSignals(JSONObject object) {
        if (voiceCallFragment != null) {
            voiceCallFragment.onRemoteMessage(object);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                LinkifyMessage msg = new LinkifyMessage(null, new Date(), picturePath, getSerial(), chatId);
                viewModel.insertMessage(msg);
                adapter.addToStart(msg, true);
                if (mService != null) {
                    mService.sendImage(picturePath);
                }
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_channel_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.voiceCall:
                if (mService != null) {

                    voiceCallFragment = VoiceCallFragment.getInstance(true,mRemoteUserName,mRemoteUserAvatar);
                    replaceFragment(voiceCallFragment, "audio");
                }
                break;
            case R.id.videoCall:
                if (mService != null) {

                    callFragment = VideoCallFragment.getInstance(true,mRemoteUserName,mRemoteUserAvatar);
                    replaceFragment(callFragment, "video");
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(Fragment fragment, String TAG) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container_layout, fragment, TAG).addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void sendMessage(String message) {

        if (mService != null) {
            mService.sendSignal(message);
        }

    }

    @Override
    public void popupFragment() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void playSound() {

        mp = MediaPlayer.create(getApplicationContext(), R.raw.skype_ring);// the song is a filename which i have pasted inside a folder **raw** created under the **res** folder.//
        mp.start();
        mp.start();
        mp.setLooping(true);

    }

    @Override
    public void stopSound() {
        if (mp != null)
            mp.release();
    }

    @Override
    public void updateCall(LinkifyCalls call) {
        call.setUserId(mRemoteUserId);
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            ChatDataBase.getInstance(this.getApplication()).myDao().insertCall(call);
        });
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}
