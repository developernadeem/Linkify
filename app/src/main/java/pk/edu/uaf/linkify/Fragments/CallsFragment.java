package pk.edu.uaf.linkify.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pk.edu.uaf.linkify.Adapter.CallsAdapter;
import pk.edu.uaf.linkify.R;


public class CallsFragment extends Fragment {
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.calls, container, false);
       RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String[] calls = {"Sumbal","Sana","Srbaz","Fatima","Hamna","Nida","Nakasha","Amna","Saira","Sidra","Salma","Sumbal","Sana","Srbaz","Fatima","Hamna","Nida","Nakasha","Amna","Saira","Sidra","Salma"};
         recyclerView.setAdapter(new CallsAdapter(calls));
        return view;
    }
}
