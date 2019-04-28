package com.sumbal.linkify.Modal;

import com.stfalcon.chatkit.commons.models.IUser;

public class LinkifyUser implements IUser {
    private  String mId,mName,mAvatar;

    public LinkifyUser(String mId, String mName, String mAvatar) {
        this.mId = mId;
        this.mName = mName;
        this.mAvatar = mAvatar;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getAvatar() {
        return mAvatar;
    }
}
