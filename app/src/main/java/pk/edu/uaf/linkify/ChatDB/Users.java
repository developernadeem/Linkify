package pk.edu.uaf.linkify.ChatDB;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.sql.Timestamp;

@Entity(tableName = "user")
public class Users {

    @PrimaryKey(autoGenerate = true)
    private int user_id;
    private String phone;
    private String userName;
    private String fullName;
    private boolean is_active;
    private Timestamp createDate;

    public Users(int user_id, String phone, String userName, String fullName, boolean is_active,Timestamp createDate) {
        this.user_id = user_id;
        this.phone = phone;
        this.userName = userName;
        this.fullName = fullName;
        this.is_active = is_active;
        this.createDate=createDate;
    }


@Ignore
    public Users(String phone, String userName, String fullName, boolean is_active,Timestamp createDate) {
        this.phone = phone;
        this.userName = userName;
        this.fullName = fullName;
        this.is_active = is_active;
        this.createDate=createDate;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

}
