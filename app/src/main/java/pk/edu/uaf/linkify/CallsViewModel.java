package pk.edu.uaf.linkify;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Modal.CallCustomResponse;

/**
 * @author Muhammad Nadeem
 * @Date 6/25/2019.
 */

/**
 * @author Muhammad Nadeem
 * @Date 5/25/2019.
 */

public class CallsViewModel extends AndroidViewModel {
    private LiveData<List<CallCustomResponse>> mCalls;
    private ChatDataBase chatDB;

    public CallsViewModel(@NonNull Application application) {
        super(application);
        chatDB = ChatDataBase.getInstance(this.getApplication());
        mCalls = chatDB.myDao().getAllCalls();
    }

    public LiveData<List<CallCustomResponse>> getAllCalls() {
        return mCalls;
    }


}
