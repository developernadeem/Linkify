package pk.edu.uaf.linkify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;

import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;

/**
 * @author Muhammad Nadeem
 * @Date 5/25/2019.
 */
public class ChatViewModel extends ViewModel {
    private LiveData<List<LinkifyMessage>> messages;
    ChatDataBase mDb;
    public ChatViewModel(ChatDataBase mDB, long mChatId) {
        this.mDb= mDB;
        messages = mDB.myDao().getUserMessages(mChatId);
    }
    public LiveData<List<LinkifyMessage>> getMessages(){
        return messages;
    }
    public void insertMessage(LinkifyMessage msg){
        AppExecutor.getInstance().getSingleThreadExecutor().submit(()->{
            mDb.myDao().insertMessage(msg);
        });
    }
    public void updateChat(long chatId, String msg, Date date){
        AppExecutor.getInstance().getSingleThreadExecutor().submit(()->{
            mDb.myDao().updateChat(chatId, msg, date);
        });
    }

}
