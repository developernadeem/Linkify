package pk.edu.uaf.linkify.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.USER_IMAGE_PATH;

public class ConnectFragment extends Fragment {
    @BindView(R.id.user_dp)
    CircleImageView userDp;
    @BindView(R.id.btn_hotspot)
    Button btnHotspot;
    @BindView(R.id.btn_wifi)
    Button btnWifi;
    @BindView(R.id.checkBox)
    CheckBox serviceCheckbox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.connect, container, false);
        ButterKnife.bind(this, view);
        if (PrefUtils.getStringPref(getContext(), USER_IMAGE_PATH) != null){
            Glide.with(this).load(PrefUtils.getStringPref(getContext(), USER_IMAGE_PATH)).into(userDp);
        }


            return view;
    }
}
