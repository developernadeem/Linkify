package pk.edu.uaf.linkify;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pk.edu.uaf.linkify.Utils.AppConstant;
import pk.edu.uaf.linkify.Utils.UtilsFunctions;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static pk.edu.uaf.linkify.Utils.AppConstant.GALLERY_REQUEST_CODE;

public class DataChannelActivity extends AppCompatActivity implements ServiceCallBacks {
    private static final String TAG = "SampleDataChannelAct";
    public static final int CHUNK_SIZE = 64000;
    private boolean isInitiator = false;
    private Socket mSocket;
    private NsdServiceInfo mInfo;
    private List<String> queueMsg = new ArrayList<>();
    private BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
    private LinkifyIntentService mService = null;
    AppExecutor appExecutor;

    int incomingFileSize;
    int currentIndexPointer;
    byte[] imageFileBytes;
    boolean receivingFile;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;

    private ChatDataBase mDb;

    private PeerConnectionFactory factory;
    private PeerConnection localPeerConnection, remotePeerConnection;
    private DataChannel localDataChannel;

    @BindView(R.id.send)Button btnSend;
    @BindView(R.id.message)EditText editText;
    @BindView(R.id.txtmymessage)TextView myLastTextView;
    @BindView(R.id.txtremotemsg)TextView remoteLastextView;
    @BindView(R.id.myImageView)
    ImageView myImageView;
    @BindView(R.id.remoteImageView)
    ImageView remoteImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_chennel);
        ButterKnife.bind(this);
        btnSend.setOnClickListener(v -> sendMessage());
        Intent intent = getIntent();

        mDb = ChatDataBase.getInstance(getApplicationContext());


        doBindService();
        appExecutor = AppExecutor.getInstance();

        initializePeerConnectionFactory();

        initializePeerConnections();
        if (intent.hasExtra("info")) {
            mInfo = intent.getParcelableExtra("info");
            isInitiator = true;
            Future<Boolean> wait = appExecutor.getNetworkExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call()  {
                    try {
                        mSocket = new Socket(mInfo.getHost(),mInfo.getPort());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
            try {
                wait.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            appExecutor.getNetworkExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    DataInputStream in = null;
                    try {
                        Log.d(TAG, "onCreate: Waiting for msgs");
                        //out = new PrintWriter(mSocket.getOutputStream(), true);

                        in = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
                        while (!Thread.interrupted()) {
                            String obj =  in.readUTF();
                            Log.d("dddddddd", "run: Receiving from Activity:"+obj);
                            if (obj == null) break;
                            JSONObject json = new JSONObject(obj);
                            if (json.getString("type").equals("answer")) {
                                Log.d(TAG, "onReceived: answer received");
                                localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, json.getString("sdp")));
//
                            } else if (json.getString("type").equals("candidate")) {
                                Log.d(TAG, "onReceived: candidate received");


                                IceCandidate candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                                localPeerConnection.addIceCandidate(candidate);

                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            assert in != null;
                            in.close();
                        } catch (Exception ignored) {

                        }

                    }
                }
            });
            appExecutor.getNetworkExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream out;
                        out = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
                        while (!Thread.currentThread().isInterrupted()) {
                            String msg = queue.take();
                            Log.d("dddddddd", "run: Sending fromActivity:"+msg);
                            out.writeUTF(msg);
                            out.writeInt(AppConstant.OFFER_CASE_MESSAGE);
                            out.flush();
                        }
                    }catch (Exception e){
                        Log.d(TAG, "run: "+e.getMessage());
                    }
                }
            });
            SendOffer();

        } else if (intent.hasExtra("json")) {
            String json = intent.getStringExtra("json");
            Log.d(TAG, "onCreate: FromService"+ json);
            isInitiator = false;

            try {
                JSONObject obj = new JSONObject(json);
                doAnswer(obj);
            } catch (Exception e) {
                Log.d(TAG, "onCreate: " + e.toString());
                //e.printStackTrace();
            }

        }

    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
    }

    private void initializePeerConnections() {
        localPeerConnection = createPeerConnection(factory);

        localDataChannel = localPeerConnection.createDataChannel("sendDataChannel", new DataChannel.Init());
        localDataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                Log.d(TAG, "onStateChange: " + localDataChannel.state().toString());
                runOnUiThread(() -> {
                    if (localDataChannel.state() == DataChannel.State.OPEN) {
                        btnSend.setEnabled(true);
                    } else {
                        btnSend.setEnabled(false);
                    }
                });
            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {

            }
        });
    }

    private void SendOffer() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        localPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: offer created");
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    Log.d(TAG, "onCreateSuccess: offer Created"+ message);
                    queue.put(message.toString());
                } catch (Exception e) {
                    Log.d(TAG, "onCreateSuccess: "+e.getMessage());
                }
            }
        }, sdpMediaConstraints);
    }
    private void doAnswer(JSONObject obj) {
        Log.d(TAG, "doAnswer: called");
        try {
            localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, obj.getString("sdp")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        localPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                try {


                    JSONObject message = new JSONObject();

                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    //use handler to send answer
                    Log.d(TAG, "onCreateSuccess: sending message via serviceS");
                    if (mService !=   null){
                        mService.sendMessageTOService(message.toString() );
                    }
                    else queueMsg.add(message.toString());

                } catch (JSONException e) {
                    Log.d(TAG, "onCreateSuccess: "+e.getMessage());
                }
            }
        }, new MediaConstraints());
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
//                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    if (isInitiator) {
                        queue.put(message.toString());
                    } else {
                        Log.d(TAG, "onIceCandidate: sending candidate via Service" + message);
                        if (mService != null) {
                            mService.sendMessageTOService(message.toString());
                        }else queueMsg.add(message.toString());
                    }
                    //sendMessage(sendMessage);
                } catch (Exception e) {
                    Log.d(TAG, "onIceCandidate: " + e.getMessage());
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: ");
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: is local: "  + " , state: " + dataChannel.state());
                dataChannel.registerObserver(new DataChannel.Observer() {
                    @Override
                    public void onBufferedAmountChange(long l) {

                    }

                    @Override
                    public void onStateChange() {
                        Log.d(TAG, "onStateChange: remote data channel state: " + dataChannel.state().toString());
                    }

                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        Log.d(TAG, "onMessage: got message");
                        readIncomingMessage(buffer.data);
                    }
                });
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    public void sendMessage() {
        String message = editText.getText().toString();
        if (message.isEmpty()) {
            return;
        }

        editText.setText("");
        myLastTextView.setText(message);

        ByteBuffer data = stringToByteBuffer("-s" + message, Charset.defaultCharset());
        localDataChannel.send(new DataChannel.Buffer(data, false));
    }

    private void readIncomingMessage(ByteBuffer buffer) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        if (!receivingFile) {
            String firstMessage = new String(bytes, Charset.defaultCharset());
            String type = firstMessage.substring(0, 2);

            if (type.equals("-i")) {
                incomingFileSize = Integer.parseInt(firstMessage.substring(2, firstMessage.length()));
                imageFileBytes = new byte[incomingFileSize];
                Log.d(TAG, "readIncomingMessage: incoming file size " + incomingFileSize);
                receivingFile = true;
            } else if (type.equals("-s")) {
                runOnUiThread(() -> remoteLastextView.setText(firstMessage.substring(2, firstMessage.length())));
            }
        } else {
            for (byte b : bytes) {
                imageFileBytes[currentIndexPointer++] = b;
            }
            if (currentIndexPointer == incomingFileSize) {
                Log.d(TAG, "readIncomingMessage: received all bytes");
                Bitmap bmp = BitmapFactory.decodeByteArray(imageFileBytes, 0, imageFileBytes.length);
                receivingFile = false;
                currentIndexPointer = 0;
                runOnUiThread(() -> remoteImageView.setImageBitmap(bmp));
            }
        }
    }

    private static ByteBuffer stringToByteBuffer(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(getBaseContext(),
                LinkifyIntentService.class), connection, Context.BIND_AUTO_CREATE);
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
            LinkifyIntentService.LocalBinder binder = (LinkifyIntentService.LocalBinder) service;

            mService = binder.getService();
            mService.setCallbacks(DataChannelActivity.this); // register

            if (!queueMsg.isEmpty())
                for (String s : queueMsg) {
                    mService.sendMessageTOService(s);
                }
            queueMsg.clear();



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "Service disconnected: " + name);

            // Reset the service messenger and connection status
            DataChannelActivity.this.mService = null;
        }
    };

    @Override
    public void getMessageFromService(JSONObject message) {
        try {
            localPeerConnection.addIceCandidate(new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate")));
        } catch (JSONException e) {
            Log.d(TAG, "getMessageFromService: " + e.getMessage());
        }
    }
    @Override
    protected void onDestroy() {

        if (!appExecutor.getNetworkExecutor().isShutdown()) {
            appExecutor.getNetworkExecutor().shutdownNow();
        }
        try {
            mSocket.close();
            mSocket = null;
        } catch (Exception e) {
            Log.d(TAG, "onDestroy: " + e.getMessage());
        }
        doUnbindService();
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.data_channel_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sendImsge:
                pickFromGallery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    File file = new File(picturePath);
                    int size = (int) file.length();
                    Log.d("vhdvjvxcvcxnvxc", "onActivityResult: "+size +" picturePath: "+picturePath);
                    byte[] bytes = UtilsFunctions.readPickedFileAsBytes(file, size);
                    sendImage(size, bytes);
                    myImageView.setImageURI(selectedImage);
                    break;
            }
    }


    private void sendImage(int size, byte[] bytes) {
        int numberOfChunks = size / CHUNK_SIZE;

        ByteBuffer meta = stringToByteBuffer("-i" + size, Charset.defaultCharset());
        localDataChannel.send(new DataChannel.Buffer(meta, false));

        for (int i = 0; i < numberOfChunks; i++) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, i * CHUNK_SIZE, CHUNK_SIZE);
            localDataChannel.send(new DataChannel.Buffer(wrap, false));
        }
        int remainder = size % CHUNK_SIZE;
        if (remainder > 0) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, numberOfChunks * CHUNK_SIZE, remainder);
            localDataChannel.send(new DataChannel.Buffer(wrap, false));
        }
    }
}
