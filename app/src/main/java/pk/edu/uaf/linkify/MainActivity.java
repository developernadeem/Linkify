package pk.edu.uaf.linkify;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    ServerSocket mServerSocket;
    NsdManager mNsdManager;
    NsdManager.RegistrationListener mRegistrationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createService = findViewById(R.id.button);
        Button discoverService = findViewById(R.id.button2);
        createService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeRegistrationListener();
                registerService(initializeServerSocket());

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
    public int initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.d("ERROR",e.toString());
            e.printStackTrace();
        }

        // Store the chosen port.
        return mServerSocket.getLocalPort();
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("Nadeem Chat");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }
    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                //mServiceName = NsdServiceInfo.getServiceName();
                Toast.makeText(MainActivity.this, "Service Registered Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
            }
        };
    }
}
