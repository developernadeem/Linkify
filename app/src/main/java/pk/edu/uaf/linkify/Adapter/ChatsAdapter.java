package pk.edu.uaf.linkify.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import pk.edu.uaf.linkify.ChatActivity;
import pk.edu.uaf.linkify.Modal.CustomResponse;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.R;

import static pk.edu.uaf.linkify.Utils.AppConstant.MESSAGE;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.Holder> {

    private Context context;
    private List<CustomResponse> chatlist;


    public ChatsAdapter(Context context, List<CustomResponse> list) {
        this.context = context;
        this.chatlist = list;
    }

    public void updateDataSet(List<CustomResponse> chats) {
        chatlist = chats;
    }

    @NonNull
    @Override
    public ChatsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.chatsitem, viewGroup, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.Holder holder, int i) {

        CustomResponse response = chatlist.get(i);
        LinkifyUser user = response.getUser();
        LinkifyChat chat = response.getChat();
        //holder.img1.setImageDrawable(context.getResources().getDrawable(user.getUserImg()));
        holder.name.setText(user.getName());
        holder.lastMessage.setText(chat.getLastMsg());
        holder.img1.setText(user.getAvatar());
        String pattern = "MM/yy HH:mm";

        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date according to the chosen pattern
        DateFormat df = new SimpleDateFormat(pattern);
        holder.lastModified.setText(df.format(chat.getLastModified()));
        GradientDrawable magnitudeCircle = (GradientDrawable) holder.img1.getBackground();
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        magnitudeCircle.setColor(color);

        holder.chatsLayout.setOnClickListener(view -> {
            //Use context or getActivty in fragments
            Intent intent = new Intent(context, ChatActivity.class);
            intent.setAction(MESSAGE);
            intent.putExtra("id",chat.getChatId());
            intent.putExtra("userId",user.getId());
            context.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {

        return chatlist.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView img1;
        TextView name, lastMessage, lastModified;
        ConstraintLayout chatsLayout;

        public Holder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.circleTextView);
            name = itemView.findViewById(R.id.name);
            lastModified = itemView.findViewById(R.id.lastModified);

            lastMessage = itemView.findViewById(R.id.text);
            chatsLayout = itemView.findViewById(R.id.chatlayout);

        }

    }


}
