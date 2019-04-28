package pk.edu.uaf.linkify.Interfaces;

public interface OnCallEvent {
    void onCallHangUp();

    void onCameraSwitch();

    void onSpeakerChange();

    boolean onToggleMic();
}