package pk.edu.uaf.linkify.ServicesAndThreads;


import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import pk.edu.uaf.linkify.CallActivity;
import pk.edu.uaf.linkify.DataChannelActivity;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.Utils.AppConstant;
import pk.edu.uaf.linkify.Utils.PrefUtils;

import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.NOTIFICATION_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;

public class LinkifyIntentService extends IntentService {
    private static final String TAG = "LinkifyIntentService";
    /**
     * ID used for notifications
     */


    /**
     * Server Socket for all incoming connections
     */
    private ServerSocket mServerSocket;
    private Socket client;
    /**
     * @mNsdManer for @NsdManager
     */
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private ServiceCallBacks serviceCallbacks;


    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public LinkifyIntentService getService() {
            // Return this instance of MyService so clients can call public methods
            return LinkifyIntentService.this;
        }
    }


    List<JSONObject> messageQueue = new ArrayList<>();
    BlockingQueue<String> candidateQue = new ArrayBlockingQueue<>(10);


    public LinkifyIntentService() {
        super("LinkifyIntentService");
        setIntentRedelivery(true);
    }

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
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent");
        initializeRegistrationListener();
        registerService(initializeServerSocket());

        try {
            client = mServerSocket.accept();
            AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                DataOutputStream objectOutputStream;
                try {
                    objectOutputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        String msg = candidateQue.take();
                        Log.d("ffffffff", "Sending from service:" + msg);
                        objectOutputStream.writeUTF(msg);
                        objectOutputStream.flush();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "run: " + e.getMessage());
                }

            });
            Log.d(TAG, "client connected: " + client.toString());
//            if (objectOutputStream!= null) {
//                objectOutputStream.reset();
//            }

            DataInputStream in = new DataInputStream(new
                    BufferedInputStream(client.getInputStream()));
            while (true) {
                String obj = in.readUTF();
                int opt = in.readInt();
                Log.d("ffffffff", " Receding from service: " + obj);
                if (obj == null) break;

                JSONObject json = new JSONObject(obj);
                if (json.getString("type").equals("offer")) {
                    Log.d(TAG, "onHandleIntent: offer Received");
                    startActivityForCall(obj, opt);
                } else if (json.getString("type").equals("candidate")) {
                    Log.d(TAG, "onHandleIntent: candidate Received");
                    //send candidate
                    if (serviceCallbacks != null) {
                        serviceCallbacks.getMessageFromService(json);
                    } else {
                        messageQueue.add(json);
                    }

                }

            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ignored) {

        }

    }

    private void startActivityForCall(String s, int opt) {
        Log.d(TAG, "startActivityForCall: s%" + opt);
        Intent dialogIntent;
        if (opt == AppConstant.OFFER_CASE_CALL) {
            dialogIntent = new Intent(getBaseContext(), CallActivity.class);
        } else
            dialogIntent = new Intent(getBaseContext(), DataChannelActivity.class);

        dialogIntent.putExtra("json", s);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(dialogIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");;
        try {
            mNsdManager.unregisterService(mRegistrationListener);
            if (client != null) client.close();
            mServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.d("ERROR", e.toString());
            e.printStackTrace();
        }

        // Store the chosen port.
        return mServerSocket.getLocalPort();
    }

    public void registerService(int port) {
        ;
        String name = PrefUtils.getStringPref(this,USER_SERVICE_NAME);


        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
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
                Toast.makeText(LinkifyIntentService.this, "Service Registered Successfully", Toast.LENGTH_LONG).show();
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallBacks callbacks) {
        serviceCallbacks = callbacks;
        if (!messageQueue.isEmpty()) {
            for (JSONObject message : messageQueue) {
                serviceCallbacks.getMessageFromService(message);
            }
            messageQueue.clear();
        }

    }

    public void sendMessageTOService(String message) {
        Log.d(TAG, "sendMessageTOService: " + message);
        try {
            candidateQue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
