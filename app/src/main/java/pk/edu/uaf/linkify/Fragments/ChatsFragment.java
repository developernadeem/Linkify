package pk.edu.uaf.linkify.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pk.edu.uaf.linkify.Adapter.ChatsAdapter;
import pk.edu.uaf.linkify.Modal.User;
import pk.edu.uaf.linkify.R;

public class ChatsFragment extends Fragment {


    public RecyclerView recyclerView;
    public List<User> list;
    public View view;
    ChatsAdapter adapter;

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

        adapter= new ChatsAdapter(getContext(),list);
        recyclerView.setAdapter(adapter);


        return view;

    }

}
