package pk.edu.uaf.linkify;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_incoming_call);
//        LottieAnimationView animationView = findViewById(R.id.pick);
//        animationView.addValueCallback(
//                new KeyPath("01_incoming call_180"),
//                LottieProperty.COLOR_FILTER,
//                frameInfo -> new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
//        );

    }
}
