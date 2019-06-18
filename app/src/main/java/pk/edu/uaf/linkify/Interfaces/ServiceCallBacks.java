package pk.edu.uaf.linkify.Interfaces;

import org.json.JSONObject;

public interface ServiceCallBacks {
    void getMessageFromService(JSONObject message);
    void getUserMessage(String msg, int type);
    void inComingVideoCall();
    void inComingVoiceCall();
}
