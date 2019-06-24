package pk.edu.uaf.linkify.Modal;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * @author Muhammad Nadeem
 * @Date 6/23/2019.
 */
@Entity(tableName = "calls")
public class LinkifyCalls {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Date date;
    private long duration;
    private boolean type;
    private int status;
    @ForeignKey(entity = LinkifyUser.class, parentColumns = "mId", childColumns = "userId")
    private String userId;

    @Ignore
    public LinkifyCalls(Date date, long duration, boolean type, int status) {
        this.date = date;
        this.duration = duration;
        this.type = type;
        this.status = status;
    }
    public LinkifyCalls(long id, Date date, long duration, boolean type, int status, String userId) {
        this.id = id;
        this.date = date;
        this.duration = duration;
        this.type = type;
        this.status = status;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
