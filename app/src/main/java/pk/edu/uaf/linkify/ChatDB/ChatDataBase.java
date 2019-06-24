package pk.edu.uaf.linkify.ChatDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pk.edu.uaf.linkify.Modal.LinkifyCalls;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.Modal.LinkifyUser;

import static pk.edu.uaf.linkify.Utils.AppConstant.DATABASE_NAME;

@Database(entities = {LinkifyUser.class, LinkifyMessage.class, LinkifyChat.class, LinkifyCalls.class},version = 3,exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class ChatDataBase extends RoomDatabase {

    private static final String TAG = ChatDataBase.class.getSimpleName();
    private static final Object LOCK=new Object();
    private static ChatDataBase sInstance;

    public static ChatDataBase getInstance(Context context){
        if (sInstance==null){
            synchronized (LOCK){
                sInstance= Room.databaseBuilder(context.getApplicationContext(),
                ChatDataBase.class,DATABASE_NAME)
                        .build();
            }

        }
        return sInstance;
    }
    public abstract MyDao myDao();
}
