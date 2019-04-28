package com.sumbal.linkify;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumbal.linkify.Adapter.chats_Adapter;
import com.sumbal.linkify.Modal.User;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {


    public RecyclerView recyclerView;
    public List<User> list;
    public View view;
    chats_Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.chats, container, false);

        list = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView_chats_id);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        User u = new User(R.drawable.item,"Sumbal","That's good");
        User u1 = new User(R.drawable.item,"Nakasha","this design looks nice");
        User u2 = new User(R.drawable.item,"Fatima","Do this in a proper way");
        User u3 = new User(R.drawable.item,"Sana","yeah wow ");
        User u4 = new User(R.drawable.item,"Maryam","Nice work");

        list.add(u);
        list.add(u1);
        list.add(u2);
        list.add(u3);
        list.add(u4);

        adapter= new chats_Adapter(getContext(),list,R.layout.chatsitem);
        recyclerView.setAdapter(adapter);


        return view;

    }

}
