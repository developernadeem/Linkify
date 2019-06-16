package pk.edu.uaf.linkify.HomeActivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Future;

import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Modal.CustomResponse;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.ServicesAndThreads.AppExecutor;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
public class MainViewModel extends AndroidViewModel {
    private LiveData<List<CustomResponse>> mUsers;
    private ChatDataBase chatDB;
    private AppExecutor executor;

    public MainViewModel(@NonNull Application application) {
        super(application);
        chatDB = ChatDataBase.getInstance(this.getApplication());
        executor = AppExecutor.getInstance();
        mUsers  = chatDB.myDao().getAllChats();
    }

    public LiveData<List<CustomResponse>> getAllChats() {
        return mUsers;
    }
    public long  insertChat(LinkifyChat user){
        Future<Long> obj = executor.getSingleThreadExecutor().submit(() -> chatDB.myDao().insertChat(user));
        try {
            return  obj.get();
        }  catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void updateChat(LinkifyChat user){
        executor.getSingleThreadExecutor().execute(()->{
            chatDB.myDao().updateChat(user);
        });
    }
    public void deleteUser(LinkifyUser user){
        executor.getSingleThreadExecutor().execute(()->{
            chatDB.myDao().deleteUser(user);
        });
    }
    public  void insertUser(LinkifyUser user){
        executor.getSingleThreadExecutor().execute(()->{
            chatDB.myDao().insertUser(user);
        });
    }
}
