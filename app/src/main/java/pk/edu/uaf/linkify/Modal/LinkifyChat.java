package pk.edu.uaf.linkify.Modal;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * @author Muhammad Nadeem
 * @Date 5/23/2019.
 */
@Entity(tableName = "chat")
public class LinkifyChat {
    @PrimaryKey(autoGenerate = true)
    private long chatId;
    @ForeignKey(entity = LinkifyUser.class, parentColumns = "mId", childColumns = "userId")
    private String userId;
    private Date lastModified;
    private int unRead;
    private String lastMsg;

    @Ignore
    public LinkifyChat(long chatId, String userId, Date lastModified, int unRead, String lastMsg) {
        this.chatId = chatId;
        this.userId = userId;
        this.lastModified = lastModified;
        this.unRead = unRead;
        this.lastMsg = lastMsg;
    }

    public LinkifyChat(String userId, Date lastModified, int unRead, String lastMsg) {
        this.userId = userId;
        this.lastModified = lastModified;
        this.unRead = unRead;
        this.lastMsg = lastMsg;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getUnRead() {
        return unRead;
    }

    public void setUnRead(int unRead) {
        this.unRead = unRead;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }
}
