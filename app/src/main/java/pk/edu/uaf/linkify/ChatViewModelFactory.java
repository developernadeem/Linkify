package pk.edu.uaf.linkify;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import pk.edu.uaf.linkify.ChatDB.ChatDataBase;

/**
 * @author Muhammad Nadeem
 * @Date 5/25/2019.
 */
public class ChatViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final ChatDataBase mDB;
    private long chatId;

    public ChatViewModelFactory(ChatDataBase mDB, long chatId) {
        this.mDB = mDB;
        this.chatId = chatId;
    }
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass){
        return (T) new ChatViewModel(mDB,chatId);
    }
}
