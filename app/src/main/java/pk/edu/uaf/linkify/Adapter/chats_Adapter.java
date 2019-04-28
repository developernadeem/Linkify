package com.sumbal.linkify.Adapter;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sumbal.linkify.ChatActivity;
import com.sumbal.linkify.Modal.User;
import com.sumbal.linkify.R;

import java.util.List;

public class chats_Adapter extends RecyclerView.Adapter<chats_Adapter.Holder> {

    Context context;
    List<User> list;
    int resorces;


    public chats_Adapter(Context context, List<User> list, int resorces) {
        this.context = context;
        this.list = list;
        this.resorces = resorces;
    }

    @NonNull
    @Override
    public chats_Adapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(resorces,null,false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull chats_Adapter.Holder holder, int i) {

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
