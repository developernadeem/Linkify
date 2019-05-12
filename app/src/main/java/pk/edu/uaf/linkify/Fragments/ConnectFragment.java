package pk.edu.uaf.linkify.Fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.USER_IMAGE_PATH;

public class ConnectFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.imagePerson)
    CircleImageView userDp;
    @BindView(R.id.btn_hotspot)
    Button btnHotspot;
    @BindView(R.id.btn_wifi)
    Button btnWifi;
    @BindView(R.id.checkBox)
    CheckBox serviceCheckbox;
    @BindView(R.id.online)
    ImageView onlineStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_connect, container, false);
        ButterKnife.bind(this, view);
        if (PrefUtils.getStringPref(getContext(), USER_IMAGE_PATH) != null){
            Glide.with(this).load(PrefUtils.getStringPref(getContext(), USER_IMAGE_PATH)).into(userDp);
        }
        btnWifi.setOnClickListener(this);
        btnHotspot.setOnClickListener(this);
        serviceCheckbox.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_wifi:
                Intent settingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(settingsIntent);
                break;
            case R.id.btn_hotspot:
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked){
                onlineStatus.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnline), android.graphics.PorterDuff.Mode.SRC_IN);

                Intent serviceIntent = new Intent(getActivity(), LinkifyIntentService.class);
                ContextCompat.startForegroundService(getActivity(), serviceIntent);
            }else {
                onlineStatus.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorDivider), android.graphics.PorterDuff.Mode.SRC_IN);
                Intent serviceIntent = new Intent(getActivity(), LinkifyIntentService.class);
                getContext().stopService(serviceIntent);
            }
        }catch (Exception ignored){
            //Exception ignore if any
        }

    }
}
