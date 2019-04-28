package com.sumbal.linkify.Modal;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class LinkyfyMessage implements IMessage {
    private String mId,mText;
    private Date mdate;
    private LinkifyUser muser;

    public LinkyfyMessage(String mId, String mText, Date mdate, LinkifyUser muser) {
        this.mId = mId;
        this.mText = mText;
        this.mdate = mdate;
        this.muser = muser;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public IUser getUser() {
        return muser;
    }

    @Override
    public Date getCreatedAt() {
        return mdate;
    }
}
