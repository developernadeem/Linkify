package pk.edu.uaf.linkify.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import pk.edu.uaf.linkify.R;

public class ConnectFragment extends Fragment {
    private ImageView  img_item;
    private Button btn_hotspot, btn_wifi;
    private CheckBox checkBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.connect, container, false);

        img_item = view.findViewById(R.id.item);
        btn_hotspot = view.findViewById(R.id.btn_hotspot);
        btn_wifi = view.findViewById(R.id.btn_wifi);
        container = view.findViewById(R.id.checkBox);


        return view;
    }
}
