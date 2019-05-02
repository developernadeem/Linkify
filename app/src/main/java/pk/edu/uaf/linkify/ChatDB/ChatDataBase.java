package pk.edu.uaf.linkify.ChatDB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Users.class,Messages.class},version = 1,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class ChatDataBase extends RoomDatabase {

    private static final String TAG = ChatDataBase.class.getSimpleName();
    private static final Object LOCK=new Object();
    private static final String DATABASE_NAME="chatdatabase";
    private static ChatDataBase sInstance;

    public static ChatDataBase getInstance(Context context){
        if (sInstance==null){
            synchronized (LOCK){
                sInstance= Room.databaseBuilder(context.getApplicationContext(),
                ChatDataBase.class,ChatDataBase.DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }

        }
        return sInstance;
    }
}
