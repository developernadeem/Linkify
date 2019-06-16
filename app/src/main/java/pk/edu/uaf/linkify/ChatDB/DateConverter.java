package pk.edu.uaf.linkify.ChatDB;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Date longTODate(Long timeStamp){
        return timeStamp == null ? null : new Date(timeStamp);
    }


    @TypeConverter
    public static Long dateTOLong(Date date){
        return date==null ? null : date.getTime();
    }
}
