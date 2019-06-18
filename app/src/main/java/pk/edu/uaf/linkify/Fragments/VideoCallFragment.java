package pk.edu.uaf.linkify.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import pk.edu.uaf.linkify.Interfaces.OnCallEvent;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.Utils.AppRTCAudioManager;

import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_DISABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_ENABLED;
import static pk.edu.uaf.linkify.Utils.AppConstant.AUDIO_TRACK_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.FPS;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_RESOLUTION_HEIGHT;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_RESOLUTION_WIDTH;
import static pk.edu.uaf.linkify.Utils.AppConstant.VIDEO_TRACK_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoCallFragment extends Fragment {

    private OnCallEvent mListener;

    private SurfaceViewRenderer surfaceView, surfaceView2;
    private EglBase rootEglBase;
    private VideoTrack videoTrackFromCamera;
    private VideoCapturer videoCapturer;
    private AudioTrack audioTrack;
    private AppRTCAudioManager audioManager;
    private PeerConnectionFactory factory;
    public VideoCallFragment() {
        // Required empty public constructor
    }
    public void initializeFactory(PeerConnectionFactory factory){
        this.factory = factory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v =inflater.inflate(R.layout.fragment_video_call, container, false);
       initializeSurfaceViews(v);
       createVideoTrackFromCameraAndShowIt();

       return v;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCallEvent) {
            mListener = (OnCallEvent) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        // This method will be called each time the audio state (number and
// type of devices) has been changed.
        audioManager = AppRTCAudioManager.create(getContext(), this::onAudioManagerChangedState
        );
        audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
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
    private void onAudioManagerChangedState() {
        // TODO(): disable video if AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }
    private void initializeSurfaceViews(View view) {

        rootEglBase = EglBase.create();
        surfaceView = view.findViewById(R.id.surface_view);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView2 = view.findViewById(R.id.surface_view2);
        surfaceView.init(rootEglBase.getEglBaseContext(), null);
        surfaceView.setEnableHardwareScaler(true);
        surfaceView.setMirror(true);

        surfaceView2.init(rootEglBase.getEglBaseContext(), null);
        surfaceView2.setEnableHardwareScaler(true);
        surfaceView2.setMirror(true);

        view.findViewById(R.id.hang_call).setOnClickListener(v -> {
            mListener.onCallHangUp();
        });
        view.findViewById(R.id.switch_camera).setOnClickListener(v -> {
            mListener.onCameraSwitch();
        });
        ImageView microphone = view.findViewById(R.id.microphone);
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



}
