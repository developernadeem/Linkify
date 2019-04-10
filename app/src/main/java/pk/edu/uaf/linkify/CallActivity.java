package pk.edu.uaf.linkify;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;

import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pub.devrel.easypermissions.EasyPermissions;

import static org.webrtc.SessionDescription.Type.OFFER;
import static pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService.MSG_SEND_ICE;
import static pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService.MSG_SEND_SDP;

public class CallActivity extends AppCompatActivity {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720;
    public static final int FPS = 30;
    private ObjectOutputStream o;
    private JSONObject candidate = null;
    private boolean isOfferSent = false;
    private boolean isCandidateSent = false;

    private static final int IS_CANDIATE = 18;
    private static final int IS_OFFER = 19;

    private static final String TAG = "CallActivty";
    private static final int RC_CALL = 111;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    /**
     * Messenger for communicating with service.
     */
    Messenger mService = null;
    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mIsBound;
    /**
     * Some text view we are using to show state information.
     */
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;

    private SurfaceViewRenderer surfaceView, surfaceView2;
    ;
    private boolean connected = false;

    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    private Socket mSocket;
    private AppExecutor executor;
    private NsdServiceInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
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
            doCall();
            executor.getNetworkExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    InputStream in;
                    ObjectInputStream i;
                    try {
                        Log.d(TAG, "onCreate: Waiting for msgs");
                        if (mSocket == null) {
                            mSocket = new Socket(mInfo.getHost(), mInfo.getPort());
                            mSocket.setKeepAlive(true);
                            o = new ObjectOutputStream(mSocket.getOutputStream());
                        }
                        //out = new PrintWriter(mSocket.getOutputStream(), true);

                        in = mSocket.getInputStream();
                        i = new ObjectInputStream(in);
                        while (true) {
                            String obj = (String) i.readObject();
                            if (obj == null) break;
                            try {
                                JSONObject json = new JSONObject(obj);
                                if (json.getString("type").equals("answer")) {
                                    Log.d(TAG, "onReceived: answer received");
                                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, json.getString("sdp")));
                                } else if (json.getString("type").equals("candidate")) {
                                    Log.d(TAG, "onReceived: candidate received");

                                    IceCandidate candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                                    peerConnection.addIceCandidate(candidate);

                                }
                            } catch (JSONException r) {
                                Log.d(TAG, "onReceived: " + r.getMessage());
                                ;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else if (intent.hasExtra("json")) {
            Log.d(TAG, "onCreate: FromService");
            String json = intent.getStringExtra("json");
            doBindService();
            isInitiator = false;
            try {
                JSONObject obj = new JSONObject(json);
                doAnswer(obj);
            } catch (JSONException e) {
                Log.d(TAG, "onCreate: " + e.toString());
                //e.printStackTrace();
            }
        }

    }

    private void startStreamingVideo() {
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
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
                    sendMessage(message, IS_OFFER);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    private void sendMessage(final JSONObject message, int from) {
        switch (from) {
            case IS_CANDIATE:
                if (!isOfferSent) {
                    isCandidateSent = true;
                    candidate = message;

                } else
                    serializeMessage(message);
                break;
            case IS_OFFER:
                if (!isCandidateSent) {
                    //send message
                    serializeMessage(message);
                    isOfferSent = true;

                } else {
                    serializeMessage(message);
                    serializeMessage(candidate);
                }
        }


    }

    private void serializeMessage(JSONObject msg) {
        executor.getSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket == null) {
                        mSocket = new Socket(mInfo.getHost(), mInfo.getPort());
                        mSocket.setKeepAlive(true);
                        Log.d(TAG, "sendMessage: socket initialized");
                        o = new ObjectOutputStream(mSocket.getOutputStream());
                    }

                    Log.d(TAG, "sendMessage: " + msg.toString());
                    o.writeObject(msg.toString());
                    o.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doAnswer(JSONObject obj) {
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                try {
                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, obj.getString("sdp")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    //use handler to send answer
                    Message msg = Message.obtain();
                    msg.what = MSG_SEND_SDP;
                    msg.obj = message.toString();
                    Log.d("dfjijijofowepoewfjewop", "Sending message to service: " + message.toString());
                    mService.send(msg);
                    msg.recycle();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
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
                        sendMessage(message, IS_CANDIATE);
                    } else {
                        Log.d("dfjijijofowepoewfjewop", "onIceCandidate: sending candidate " + message);
                        Message msg = Message.obtain();
                        msg.what = MSG_SEND_ICE;
                        msg.obj = message.toString();
                        mService.send(msg);
                        msg.recycle();
                    }
                    //sendMessage(sendMessage);
                } catch (JSONException e) {
                    Log.d("dfjijijofowepoewfjewop", "onIceCandidate: " +e.getMessage());
                } catch (RemoteException e) {
                    Log.d("dfjijijofowepoewfjewop", "onIceCandidate: " +e.getMessage());
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
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
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
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
            CallActivity.this.mService = new Messenger(service);
            CallActivity.this.connected = true;

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        LinkifyIntentService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "Service disconnected: " + name);

            // Reset the service messenger and connection status
            CallActivity.this.mService = null;
            CallActivity.this.connected = false;
        }
    };

    private static class IncomingHandler extends Handler {
        private WeakReference<CallActivity> activityWeakReference;

        public IncomingHandler(CallActivity activityWeakReference) {
            this.activityWeakReference = new WeakReference<>(activityWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_ICE:
                    activityWeakReference.get().receiveMsgFromSerive(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void receiveMsgFromSerive(Message msg) {
        Log.d(TAG, "Received from service: " + msg.arg1);
        JSONObject json = (JSONObject) msg.obj;
        IceCandidate candidate = null;
        try {
            candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
            peerConnection.addIceCandidate(candidate);
        } catch (JSONException e) {
            Log.d(TAG, "receiveMsgFromSerive: " + e.getMessage());
        }
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(CallActivity.this,
                LinkifyIntentService.class), connection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d(TAG, "doBindService: Binding.");
    }

    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            LinkifyIntentService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(connection);
            mIsBound = false;
            Log.d(TAG, "doUnbindService: Unbinding.");
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }
}
