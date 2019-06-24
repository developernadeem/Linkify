package pk.edu.uaf.linkify.Interfaces;

import pk.edu.uaf.linkify.Modal.LinkifyCalls;

/**
 * @author Muhammad Nadeem
 * @Date 6/22/2019.
 */
public interface CallFragmentEvents {
    void sendMessage(String message);
    void popupFragment();
    void playSound();
    void stopSound();
    void updateCall(LinkifyCalls call);
}
