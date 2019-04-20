package pk.edu.uaf.linkify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MycustomTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputtextool);
    }
}
