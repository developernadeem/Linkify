package pk.edu.uaf.linkify.ChatDB;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pk.edu.uaf.linkify.ChatDB.Messages;

public interface MessageDAO
{

   @Query("Select * from message ORDER BY dateTime")
   List<Messages> loadAllMessages();

    @Insert
    void insertMessage(Messages message);

    @Update
    void updateMessage(Messages messsage);

    @Delete
    void delteMessage(Messages message);

}
