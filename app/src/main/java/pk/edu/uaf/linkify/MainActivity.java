package pk.edu.uaf.linkify;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pk.edu.uaf.linkify.ServicesAndThreads.LinkifyIntentService;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createService = findViewById(R.id.button);
        Button stopService = findViewById(R.id.stopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, LinkifyIntentService.class);
                //startActivity(serviceIntent);

                stopService(serviceIntent);

            }
        });
        Button discoverService = findViewById(R.id.button2);
        createService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, LinkifyIntentService.class);

                ContextCompat.startForegroundService(MainActivity.this, serviceIntent);


            }
        });
        discoverService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,DiscoverService.class);
                startActivity(i);
            }
        });
    }

}
