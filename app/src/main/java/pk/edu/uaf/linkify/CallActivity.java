package pk.edu.uaf.linkify;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pub.devrel.easypermissions.EasyPermissions;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class CallActivity extends AppCompatActivity implements ServiceCallBacks {


    private static final String TAG = "gfiyfyfyfyfyfyfyfy";


    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;

    private static final int RC_CALL = 111;
    private List<String> queueMsg = new ArrayList<>();
    BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);


    /**
     * Messenger for communicating with service.
     */
    private LinkifyIntentService mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    /**
     * Some text view we are using to show state information.
     */
    private boolean isInitiator = false;

    private SurfaceViewRenderer surfaceView, surfaceView2;
    ;
    private boolean connected = false;

    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    private AudioTrack audioTrack;
    private Socket mSocket;
    private AppExecutor executor;
    private NsdServiceInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        doBindService();
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
        Intent intent = getIntent();


        executor = AppExecutor.getInstance();
        //connectToSignallingServer();

        initializeSurfaceViews();

        initializePeerConnectionFactory();

        createVideoTrackFromCameraAndShowIt();

        initializePeerConnections();

        startStreamingVideo();
        if (intent.hasExtra("info")) {
            mInfo = intent.getParcelableExtra("info");
            isInitiator = true;
            Future<Boolean> wait = executor.getNetworkExecutor().submit(new Callable<Boolean>() {
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
            executor.getNetworkExecutor().execute(new Runnable() {
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
                                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, json.getString("sdp")));
//
                                } else if (json.getString("type").equals("candidate")) {
                                    Log.d(TAG, "onReceived: candidate received");


                                    IceCandidate candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                                    peerConnection.addIceCandidate(candidate);

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
            executor.getNetworkExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataOutputStream out;
                        out = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
                        while (!Thread.currentThread().isInterrupted()) {
                            String msg = queue.take();
                            Log.d("dddddddd", "run: Sending fromActivity:"+msg);
                            out.writeUTF(msg);
                            out.flush();
                        }
                    }catch (Exception e){
                        Log.d(TAG, "run: "+e.getMessage());
                    }
                }
            });
            doCall();

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

    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(audioTrack);
        mediaStream.addTrack(videoTrackFromCamera);
        peerConnection.addStream(mediaStream);

    }

    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: offer created");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    Log.d(TAG, "onCreateSuccess: offercreated"+ message);
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
            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, obj.getString("sdp")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
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

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        //iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
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
                Log.d(TAG, "onIceConnectionReceivingChange: ");
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
                Log.d(TAG, "onAddStream: " + mediaStream.audioTracks.size());
                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                remoteVideoTrack.setEnabled(true);
                remoteVideoTrack.addRenderer(new VideoRenderer(surfaceView2));

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(CallActivity.this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(new VideoRenderer(surfaceView));
        //audio traces
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID,audioSource);
    }

    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        surfaceView = findViewById(R.id.surface_view);
        surfaceView2 = findViewById(R.id.surface_view2);
        surfaceView.init(rootEglBase.getEglBaseContext(), null);
        surfaceView.setEnableHardwareScaler(true);
        surfaceView.setMirror(true);

        surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        surfaceView2.setEnableHardwareScaler(true);
        surfaceView2.setMirror(true);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    //service related work
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Set the service messenger and connected status
            // cast the IBinder and get MyService instance
            LinkifyIntentService.LocalBinder binder = (LinkifyIntentService.LocalBinder) service;

            mService = binder.getService();
            mService.setCallbacks(CallActivity.this); // register

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
            CallActivity.this.mService = null;
            CallActivity.this.connected = false;
        }
    };

    @Override
    public void getMessageFromService(JSONObject message) {
        try {
            peerConnection.addIceCandidate(new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate")));
        } catch (JSONException e) {
            Log.d(TAG, "getMessageFromService: " + e.getMessage());
        }
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


    @Override
    protected void onDestroy() {

        if (!executor.getNetworkExecutor().isShutdown()) {
            executor.getNetworkExecutor().shutdownNow();
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


}
