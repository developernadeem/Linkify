package pk.edu.uaf.linkify.Modal;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.stfalcon.chatkit.commons.models.IUser;
@Entity(tableName = "user")
public class LinkifyUser implements IUser {
    @NonNull
    @PrimaryKey
    private  String mId;
    @NonNull
    private String mName;
    @NonNull
    private String mAvatar;
    private String mPhone;

    public LinkifyUser(@NonNull String mId,@NonNull String mName,@NonNull String mAvatar, String mPhone) {
        this.mId = mId;
        this.mName = mName;
        this.mAvatar = mAvatar;
        this.mPhone = mPhone;
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

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }
}
