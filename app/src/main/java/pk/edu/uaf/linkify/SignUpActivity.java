package pk.edu.uaf.linkify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NAME;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NUMBER;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.sign_btn)
    Button btnSignIn;
    @BindView(R.id.edit_name)
    TextInputEditText editName;
    @BindView(R.id.edit_bumber)
    TextInputEditText editNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.bind(this);
        btnSignIn.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String number = editNumber.getText().toString().trim();
            PrefUtils.setStringPref(this, USER_NAME, name);
            PrefUtils.setStringPref(this, USER_NUMBER, number);
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();

        });

    }
}
