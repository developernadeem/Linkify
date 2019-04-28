package pk.edu.uaf.linkify.Adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import pk.edu.uaf.linkify.ChatActivity;
import pk.edu.uaf.linkify.Modal.User;
import pk.edu.uaf.linkify.R;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.Holder> {

    Context context;
    List<User> list;


    public ChatsAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatsAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.chatsitem,null,false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.Holder holder, int i) {

        User user = list.get(i);
        holder.img1.setImageDrawable(context.getResources().getDrawable(user.getUserImg()));
        holder.name.setText(user.getName());
        holder.text.setText(user.getText());

        holder.chatsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Use context or getActivty in fragments
                Intent intent = new Intent(context, ChatActivity.class);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView img1;
        TextView name,text;
        ConstraintLayout chatsLayout;

        public Holder(@NonNull View itemView) {
            super(itemView);

            img1 = itemView.findViewById(R.id.imageView);
            name = itemView.findViewById(R.id.name);
            text = itemView.findViewById(R.id.text);
            chatsLayout = (ConstraintLayout) itemView.findViewById(R.id.chatlayout);

        }

    }



}
