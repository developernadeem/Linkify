package pk.edu.uaf.linkify.ChatDB;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import pk.edu.uaf.linkify.Modal.User;

import static androidx.room.OnConflictStrategy.REPLACE;
@Dao
public interface MyDao
{

   @Query("Select * from message ORDER BY dateTime")
   List<Messages> loadAllMessages();

    @Insert
    void insertMessage(Messages message);

    @Update
    void updateMessage(Messages messsage);

    @Delete
    void deleteMessage(Messages message);
    @Insert
    void insertUser(User user);

    @Update(onConflict = REPLACE)
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

}
