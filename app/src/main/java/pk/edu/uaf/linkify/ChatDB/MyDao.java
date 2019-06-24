package pk.edu.uaf.linkify.ChatDB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import pk.edu.uaf.linkify.Modal.CallCustomResponse;
import pk.edu.uaf.linkify.Modal.CustomResponse;
import pk.edu.uaf.linkify.Modal.LinkifyCalls;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.Modal.LinkifyUser;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MyDao {
    @Query("SELECT chat.*,user.*  FROM chat INNER JOIN user ON chat.userId=user.mId")
    LiveData<List<CustomResponse>> getAllChats();

    @Insert
    long insertChat(LinkifyChat chat);

    @Update(onConflict = REPLACE)
    void updateChat(LinkifyChat chat);

    @Delete
    void deleteChat(LinkifyChat chat);


    @Insert(onConflict = REPLACE)
    void insertUser(LinkifyUser user);

    @Insert(onConflict = REPLACE)
    void insertMessage(LinkifyMessage msg);

    @Update(onConflict = REPLACE)
    int updateUser(LinkifyUser user);

    @Delete
    void deleteUser(LinkifyUser user);

    @Query("SELECT * FROM USER")
    LiveData<List<LinkifyUser>> getAllUsers();

    @Query("SELECT * FROM user WHERE mId= :userId")
    LinkifyUser searchUser(String userId);

    @Query("select * from message where chatid = :chatId order by mDate desc")
    LiveData<List<LinkifyMessage>> getUserMessages(long chatId);

    @Query("SELECT chatId FROM chat where userId=:id ")
    int searchChatByUserId(String id);

    @Query("Update chat set lastMsg = :msg ,lastModified=:date where chatId=:chatId ")
    void updateChat(long chatId, String msg, Date date);

    @Insert
    void insertCall(LinkifyCalls calls);
    @Query("SELECT calls.*,user.*  FROM calls INNER JOIN user ON calls.userId=user.mId")
    LiveData<List<CallCustomResponse>> getAllCalls();

}
