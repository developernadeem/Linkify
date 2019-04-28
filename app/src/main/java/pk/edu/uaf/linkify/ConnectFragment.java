package com.sumbal.linkify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ConnectFragment extends Fragment {
    ImageView  img_item;
    Button btn_hotspot, btn_wifi;
    CheckBox checkBox;

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
