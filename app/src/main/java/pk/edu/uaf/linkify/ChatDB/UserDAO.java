package pk.edu.uaf.linkify.ChatDB;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserDAO {

    @Insert
    void insertUser(Users user);

    @Update(onConflict = REPLACE)
    void updateUser(Users user);

    @Delete
    void deleteUser(Users user);

}
