package pk.edu.uaf.linkify.ChatDB;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

public class DateConverter {

    @TypeConverter
    public static Timestamp toLongDate(Long timeStamp){
        return timeStamp == null ? null : new Timestamp(timeStamp);
    }


    @TypeConverter
    public static Long toTimeStamp(Timestamp date){
        return date==null ? null : date.getTime();
    }
}
