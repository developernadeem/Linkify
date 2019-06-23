package pk.edu.uaf.linkify.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;

import java.util.ArrayList;
import java.util.List;

import pk.edu.uaf.linkify.Interfaces.CallFragmentEvents;
import pk.edu.uaf.linkify.Interfaces.OnCallEvent;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pk.edu.uaf.linkify.SimpleSdpObserver;
import pk.edu.uaf.linkify.Utils.AppRTCAudioManager;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_ANSWER;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_CANDIDATE;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_DISABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_ENABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_OFFER;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_TRACK_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class VoiceCallFragment extends Fragment implements OnCallEvent {

    private static final String TAG = VoiceCallFragment.class.getSimpleName();
    public VoiceCallFragment() {
        // Required empty public constructor
    }

    private CallFragmentEvents callFragmentEvents;

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
    private boolean isInitiated = false;
    private boolean isAnswered = false;
    private JSONObject offer = null;

    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;
    private AudioTrack audioTrack;
    private AppRTCAudioManager audioManager;
    private List<JSONObject> icecandidates = new ArrayList<>();

    private static final String IS_CALLEE_KEY = "is_callee";
    private boolean isCallee;



    public static VoiceCallFragment getInstance(boolean isCallee) {
        VoiceCallFragment fragment = new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_CALLEE_KEY, isCallee);
        fragment.setArguments(bundle);
        return fragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        isCallee = bundle.getBoolean(IS_CALLEE_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_voice_call, container, false);
        initializeSurfaceViews(v);
        initializePeerConnectionFactory();
        createVideoTrackFromCameraAndShowIt();
        initializePeerConnections();
        startStreamingVideo();
        if (isCallee) {
            doCall();
        }
        if (!isAnswered) {

            isInitiated = true;
            if (offer != null) {
                doAnswer(offer);
                if (!icecandidates.isEmpty()){
                    for (JSONObject json : icecandidates) {
                        IceCandidate candidate = null;
                        try {
                            candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                            peerConnection.addIceCandidate(candidate);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    icecandidates.clear();
                }
            }

        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    private void startStreamingVideo() {
        Log.d(TAG, "startStreamingVideo: ");
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(audioTrack);
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
                    message.put("type", AUDIO_OFFER);
                    message.put("sdp", sessionDescription.description);
                    Log.d(TAG, "onCreateSuccess: offercreated" + message);
                    callFragmentEvents.sendMessage(message.toString());
                } catch (Exception e) {
                    Log.d(TAG, "onCreateSuccess: " + e.getMessage());
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

                    message.put("type", AUDIO_ANSWER);
                    message.put("sdp", sessionDescription.description);
                    //use handler to send answer
                    Log.d(TAG, "onCreateSuccess: sending message via serviceS");
                    callFragmentEvents.sendMessage(message.toString());
                    //else
                    //queueMsg.add(message.toString());

                } catch (JSONException e) {
                    Log.d(TAG, "onCreateSuccess: " + e.getMessage());
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
                    message.put("type", AUDIO_CANDIDATE);
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    Log.d(TAG, "onIceCandidate: sending candidate " + message);
                    callFragmentEvents.sendMessage(message.toString());
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
                AudioTrack remoteVideoTrack = mediaStream.audioTracks.get(0);
                remoteVideoTrack.setEnabled(true);

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
        PeerConnectionFactory.initializeAndroidGlobals(getContext(), true, true, true);
        factory = new PeerConnectionFactory(null);
    }

    private void createVideoTrackFromCameraAndShowIt() { ;
        //audio traces
        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        // This method will be called each time the audio state (number and
// type of devices) has been changed.
        audioManager = AppRTCAudioManager.create(getContext(), () -> onAudioManagerChangedState()
        );
        audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
    }

    private void initializeSurfaceViews(View view) {


        view.findViewById(R.id.hang_call).setOnClickListener(v -> {
            onCallHangUp();
        });
        view.findViewById(R.id.switch_camera).setOnClickListener(v -> {
            onCameraSwitch();
        });
        ImageView microphone = view.findViewById(R.id.microphone);
        microphone.setOnClickListener(v -> {
            switch (microphone.getTag().toString()) {
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
            videoCapturer = createCameraCapturer(new Camera2Enumerator(getContext()));
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
        return Camera2Enumerator.isSupported(getContext());
    }

    //service related work


    @Override
    public void onDestroy() {

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
//            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
//            cameraVideoCapturer.switchCamera(null);
        }
    }

    @Override
    public void onSpeakerChange() {

    }

    @Override
    public boolean onToggleMic() {
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

    }

    private void onAudioManagerChangedState() {
        // TODO(): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {

            callFragmentEvents = (CallFragmentEvents) context;
        } catch (Exception ignored) {

        }
    }

    public void onRemoteMessage(JSONObject json) {
        try {
            switch (json.getString("type")) {
                case AUDIO_ANSWER:
                    Log.d(TAG, "onReceived: answer received");
                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, json.getString("sdp")));
//
                    break;
                case AUDIO_CANDIDATE:
                    Log.d(TAG, "onReceived: candidate received");
                    if (isAnswered) {
                        IceCandidate candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                        peerConnection.addIceCandidate(candidate);
                    }else
                        icecandidates.add(json);
                    break;
                case AUDIO_OFFER:
                    Log.d(TAG, "onReceived: offer received");
                    if (isInitiated) {
                        doAnswer(json);
                        isAnswered = true;
                    }else
                        offer = json;
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
