package com.sumbal.linkify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sumbal.linkify.Adapter.CallsAdapter;

public class CallsFragment extends Fragment {
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.calls, container, false);
       RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String[] calls = {"sumbal","sana","arbaz","Fatima","Hamna","Nida","Nakasha","Amna","saira","sidra","salma","sumbal","sana","arbaz","Fatima","Hamna","Nida","Nakasha","Amna","saira","sidra","salma"};
         recyclerView.setAdapter(new CallsAdapter(calls));
        return view;
    }
}
