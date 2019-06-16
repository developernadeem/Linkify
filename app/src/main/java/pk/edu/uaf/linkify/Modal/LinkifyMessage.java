package pk.edu.uaf.linkify.Modal;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

@Entity(tableName = "message")
public class LinkifyMessage implements IMessage, MessageContentType.Image {
    @PrimaryKey(autoGenerate = true)
    private int mId;
    private String mText;
    private Date mDate;
    private String mUri;
    @ForeignKey(entity = LinkifyUser.class,parentColumns = "mId",childColumns = "userId")
    private String userId;
    @ForeignKey(entity = LinkifyChat.class,parentColumns = "chatId",childColumns = "chatid")
    private long chatid;
    @Ignore
    public LinkifyMessage(String mText, Date mDate, String mUri, String userId ,long chatid) {
        this.mText = mText;
        this.mDate = mDate;
        this.mUri = mUri;
        this.userId = userId;
        this.chatid = chatid;
    }

    public LinkifyMessage(int mId, String mText, Date mDate, String mUri, String userId,long chatid) {
        this.mId = mId;
        this.mText = mText;
        this.mDate = mDate;
        this.mUri = mUri;
        this.userId = userId;
        this.chatid = chatid;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public Date getDate() {
        return mDate;
    }

    @Override
    public String getUri() {
        return mUri;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public String getmUri() {
        return mUri;
    }

    public void setmUri(String mUri) {
        this.mUri = mUri;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getChatid() {
        return chatid;
    }

    public void setChatid(long chatid) {
        this.chatid = chatid;

    }

    @Nullable
    @Override
    public String getImageUrl() {
        return mUri == ""? null:mUri;
    }
}
