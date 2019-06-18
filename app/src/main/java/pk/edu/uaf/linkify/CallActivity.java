package pk.edu.uaf.linkify;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
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

import java.util.ArrayList;

import pk.edu.uaf.linkify.Interfaces.OnCallEvent;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pk.edu.uaf.linkify.Utils.AppRTCAudioManager;

import static org.webrtc.SessionDescription.Type.OFFER;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_DISABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_ENABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_TRACK_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.FPS;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_RESOLUTION_HEIGHT;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_RESOLUTION_WIDTH;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_TRACK_ID;

public class CallActivity extends AppCompatActivity implements ServiceCallBacks, OnCallEvent {


    private static final String TAG = CallActivity.class.getSimpleName();


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

    private PeerConnection peerConnection;
    private VideoCapturer videoCapturer;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    private AudioTrack audioTrack;
    private AppRTCAudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_call);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        doBindService();
        Intent intent = getIntent();

        //connectToSignallingServer();

        initializeSurfaceViews();

        initializePeerConnectionFactory();

        createVideoTrackFromCameraAndShowIt();

        initializePeerConnections();

        startStreamingVideo();
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
                    //queue.put(message.toString());
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
                    //else
                        //queueMsg.add(message.toString());

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
                        //queue.put(message.toString());
                    } else {
                        Log.d(TAG, "onIceCandidate: sending candidate via Service" + message);
                        if (mService != null) {
                            mService.sendMessageTOService(message.toString());
                        }
                        //else queueMsg.add(message.toString());
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
        videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(new VideoRenderer(surfaceView));
        //audio traces
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID,audioSource);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
                    // This method will be called each time the audio state (number and
                    // type of devices) has been changed.
                    @Override
                    public void run() {
                        onAudioManagerChangedState();
                    }
                }
        );
        audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
    }

    private void initializeSurfaceViews() {

        rootEglBase = EglBase.create();
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView2 = findViewById(R.id.surface_view2);
        surfaceView.init(rootEglBase.getEglBaseContext(), null);
        surfaceView.setEnableHardwareScaler(true);
        surfaceView.setMirror(true);

        surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        surfaceView2.setEnableHardwareScaler(true);
        surfaceView2.setMirror(true);

        findViewById(R.id.hang_call).setOnClickListener(v -> {
            onCallHangUp();
        });
        findViewById(R.id.switch_camera).setOnClickListener(v -> {
            onCameraSwitch();
        });
        ImageView microphone = findViewById(R.id.microphone);
        microphone.setOnClickListener(v->{
            switch (microphone.getTag().toString()){
                case AUDIO_ENABLED:
                    audioTrack.setEnabled(false);
                    microphone.setTag(AUDIO_DISABLED);
                    break;
                case AUDIO_DISABLED:
                    audioTrack.setEnabled(true);
                    microphone.setTag(AUDIO_ENABLED);
                    break;

            }
        });

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

//            if (!queueMsg.isEmpty())
//                for (String s : queueMsg) {
//                    mService.sendMessageTOService(s);
//                }
//            queueMsg.clear();



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "Service disconnected: " + name);

            // Reset the service messenger and connection status
            CallActivity.this.mService = null;
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

    @Override
    public void getUserMessage(String msg,int type) {

    }

    @Override
    public void inComingVideoCall() {

    }

    @Override
    public void inComingVoiceCall() {

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
        doUnbindService();
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();
        super.onDestroy();
    }


    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        if (peerConnection != null) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        }
    }

    @Override
    public void onSpeakerChange() {

    }

    @Override
    public boolean onToggleMic()
    {
        //TODO: implement mute mic
        return false;
    }
    private void disconnect() {
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        surfaceView.release();
        surfaceView2.release();

        finish();
    }
    private void onAudioManagerChangedState() {
        // TODO(): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }
}
