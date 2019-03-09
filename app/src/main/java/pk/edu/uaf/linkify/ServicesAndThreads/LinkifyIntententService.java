package pk.edu.uaf.linkify.ServicesAndThreads;


import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;


import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;

public class LinkifyIntententService extends IntentService {
    private static final String TAG = "LinkifyIntententService";
    public static final int ID = 11;
    private ServerSocket mServerSocket;
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;

    private PowerManager.WakeLock wakeLock;

    public LinkifyIntententService() {
        super("LinkifyIntententService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        initializeRegistrationListener();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "ExampleApp:Wakelock");
        wakeLock.acquire();
        Log.d(TAG, "Wakelock acquired");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Linkify Background Service")
                    .setContentText("Running...")
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .build();

            startForeground(ID, notification);
        }


    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        registerService(initializeServerSocket());

        while(true){
            ClientWorker w;
            try{
            //server.accept returns a client connection
                w = new ClientWorker(mServerSocket.accept(), this);
                Thread t = new Thread(w);
                t.start();
            } catch (IOException e) {
//                System.out.println("Accept failed: 4444");
//                System.exit(-1);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        wakeLock.release();
        Log.d(TAG, "Wakelock released");
        mNsdManager.unregisterService(mRegistrationListener);
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Log.d(TAG, "registerService: Should be registered");
    }
    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                //mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "onServiceRegistered: Service Registered Successfully");
                Toast.makeText(LinkifyIntententService.this, "Service Registered Successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onRegistrationFailed: "+errorCode);
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
