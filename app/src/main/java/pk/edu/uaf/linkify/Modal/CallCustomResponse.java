package pk.edu.uaf.linkify.Modal;

import androidx.room.Embedded;

/**
 * @author Muhammad Nadeem
 * @Date 6/25/2019.
 */
public class CallCustomResponse {
    @Embedded
    LinkifyCalls calls;
    @Embedded
    LinkifyUser user;

    public LinkifyCalls getCalls() {
        return calls;
    }

    public void setCalls(LinkifyCalls calls) {
        this.calls = calls;
    }

    public LinkifyUser getUser() {
        return user;
    }

    public void setUser(LinkifyUser user) {
        this.user = user;
    }
}
