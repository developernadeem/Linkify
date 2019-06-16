package pk.edu.uaf.linkify.Modal;

import androidx.room.Embedded;

/**
 * @author Muhammad Nadeem
 * @Date 5/25/2019.
 */
public class CustomResponse {
    @Embedded
    LinkifyChat chat;
    @Embedded
    LinkifyUser user;

    public LinkifyChat getChat() {
        return chat;
    }

    public void setChat(LinkifyChat chat) {
        this.chat = chat;
    }

    public LinkifyUser getUser() {
        return user;
    }

    public void setUser(LinkifyUser user) {
        this.user = user;
    }
}
