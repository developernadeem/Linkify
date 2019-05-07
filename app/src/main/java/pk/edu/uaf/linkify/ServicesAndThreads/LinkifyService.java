package pk.edu.uaf.linkify.ServicesAndThreads;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.core.app.NotificationCompat;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.ACTION_START_SERVICE;
import static pk.edu.uaf.linkify.Utils.AppConstant.NOTIFICATION_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NAME;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_NUMBER;

/**
* @author Muhammad Nadeem
 *
*/

public class LinkifyService extends Service {

    private static final String TAG = "LinkifyService";
    /**
     * Server Socket for all incoming connections
     */
    private ServerSocket mServerSocket;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    /**
     * @mNsdManer for @NsdManager
     */
    private NsdManager mNsdManager;
    /*
    * Listener For NSD service callbacks
    */
    private NsdManager.RegistrationListener mRegistrationListener;
    /**
     * Blocking queue for initializer
     */
    BlockingQueue<String> queue = new LinkedBlockingDeque<>(10);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Linkify Background Service")
                    .setContentText("Running...")
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        assert action != null;
        switch (action){
            case ACTION_START_SERVICE:
                initializeRegistrationListener();
                registerService(initializeServerSocket());
                startListening();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public LinkifyService getService() {
            // Return this instance of MyService so clients can call public methods
            return LinkifyService.this;
        }
    }
    /*
    * Preparing callbacks for service registration
    */

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                //mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "onServiceRegistered: Service Registered Successfully");
                Toast.makeText(LinkifyService.this, "Service Registered Successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "onRegistrationFailed: " + errorCode);
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
    /**
    * Register NSD service to network so other people can discover
    * @param port is public port on which service will listen
    */
    public void registerService(int port) {
        ;
        String name = PrefUtils.getStringPref(this,USER_NAME)
                +"/"+PrefUtils.getStringPref(this,USER_NUMBER)
                +"/"+ Build.SERIAL;

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        Log.d(TAG, "registerService: Should be registered");
    }
    /**
     *Initializing server socket
     * port 0 mean get any available port
     */
    public int initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.wtf(TAG, "initializeServerSocket: ",e );
            e.printStackTrace();
        }

        // Store the chosen port.
        return mServerSocket.getLocalPort();
    }
    private void startListening(){
        AppExecutor.getInstance().getNetworkExecutor().execute(() -> {
            while (true){
                try {
                    Socket client =mServerSocket .accept();
                    ClientWorker clientWorker = new ClientWorker(client,queue);
                } catch (IOException e) {
                    Log.wtf(TAG, "Exception: ", e);
                    e.printStackTrace();
                    break;
                }
            }
        });
    }
    /**
    * Public method for Connection to a peer discovered by this user
     * @param info contains network information for user
    */
    public void connectToSocket(NsdServiceInfo info){
        AppExecutor.getInstance().getNetworkExecutor().execute(()->{
            try {
                Socket peer = new Socket(info.getHost(), info.getPort());
                new ClientWorker(peer, queue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
