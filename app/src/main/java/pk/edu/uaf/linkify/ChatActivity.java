package pk.edu.uaf.linkify;

import android.os.Bundle;
import android.widget.ImageView;


import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.Modal.LinkyfyMessage;

public class ChatActivity extends AppCompatActivity {
    com.stfalcon.chatkit.messages.MessagesList list;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        list =  findViewById(R.id.messagesList);




        LinkifyUser sumbal  = new LinkifyUser("1","Sumbal","SN");
        LinkifyUser nakasha = new LinkifyUser("2","Nakasha","NA");
        LinkyfyMessage msg2 = new LinkyfyMessage("2","Hi Nakasha",new Date(),nakasha);
        LinkyfyMessage msg1 = new LinkyfyMessage("1","Hi Sumbal",new Date(),sumbal);
        LinkyfyMessage msg3 = new LinkyfyMessage("3","How are you?",new Date(),nakasha);
        LinkyfyMessage msg4 = new LinkyfyMessage("4","I am good",new Date(),sumbal);
        LinkyfyMessage msg5 = new LinkyfyMessage("5","great",new Date(),nakasha);
        LinkyfyMessage msg6 = new LinkyfyMessage("6","What'going on?",new Date(),nakasha);
        MessagesListAdapter<LinkyfyMessage> adapter = new MessagesListAdapter<>("2", new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {

            }
        });
        list.setAdapter(adapter);
        adapter.addToStart(msg2,true);
        adapter.addToStart(msg1,true);
        adapter.addToStart(msg3,true);
        adapter.addToStart(msg4,true);
        adapter.addToStart(msg5,true);
        adapter.addToStart(msg6,true);
    }
}
