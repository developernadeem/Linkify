package pk.edu.uaf.linkify.Fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyService;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.ACTION_START_SERVICE;
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
        if (isMyServiceRunning()){
            onlineStatus.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorOnline), android.graphics.PorterDuff.Mode.SRC_IN);
            serviceCheckbox.setChecked(true);
        }
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

                Intent serviceIntent = new Intent(getActivity(), LinkifyService.class);
                serviceIntent.setAction(ACTION_START_SERVICE);
                ContextCompat.startForegroundService(getActivity(), serviceIntent);
            }else {
                onlineStatus.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorDivider), android.graphics.PorterDuff.Mode.SRC_IN);
                Intent serviceIntent = new Intent(getActivity(), LinkifyService.class);
                getContext().stopService(serviceIntent);
            }
        }catch (Exception ignored){
            //Exception ignore if any
        }

    }
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LinkifyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
