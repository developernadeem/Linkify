package pk.edu.uaf.linkify.ChatDB;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "message")
public class Messages  {

    @PrimaryKey(autoGenerate = true)
    private int messageId;
    private int senderId;
    private Timestamp dateTime;
    private String message;
    private int recipientId;

    @Ignore
    public Messages(int senderId, Timestamp dateTime, String message, int recipientId) {
        this.senderId = senderId;
        this.dateTime = dateTime;
        this.message = message;
        this.recipientId = recipientId;
    }

    public Messages(int messageId, int senderId, Timestamp dateTime, String message, int recipientId) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.dateTime = dateTime;
        this.message = message;
        this.recipientId = recipientId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }
}
