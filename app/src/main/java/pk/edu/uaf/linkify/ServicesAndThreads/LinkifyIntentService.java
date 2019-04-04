package pk.edu.uaf.linkify.ServicesAndThreads;


import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import pk.edu.uaf.linkify.CallActivity;

import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;

public class LinkifyIntentService extends IntentService {
    private static final String TAG = "LinkifyIntentService";
    /** ID used for notifications*/
    public static final int ID = 11;
    /**
    *Server Socket for all incoming connections
    */
    private ServerSocket mServerSocket;
    private  Socket client;
    /**
    * @mNsdManer for @NsdManager
    */
    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private  Messenger messenger = new Messenger(new IncomingHandler(this));
    private PowerManager.WakeLock wakeLock;

    private final IBinder binder = new ServiceBinder();
    /** Keeps track of all current registered clients. */
    private  static ArrayList<Messenger> mClients = new ArrayList<>();
    /** Holds last value set by a client. */
    int mValue = 0;
    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.
     */
    public static final int MSG_SEND_SDP = 3;
    public static final int MSG_SEND_ICE = 4;

    public LinkifyIntentService() {
        super("LinkifyIntentService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

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
        initializeRegistrationListener();
        registerService(initializeServerSocket());

        try {
            client = mServerSocket.accept();
            Log.d(TAG, "onHandleIntent: "+client.toString());
            InputStream in = client.getInputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            while (true){
                String obj= (String) objectInputStream.readObject();
                Log.d(TAG, "onHandleIntent: "+obj);
                if (obj == null) break;
                try {
                    JSONObject json = new JSONObject(obj);
                    if (json.getString("type").equals("offer")){
                        Log.d(TAG, "onHandleIntent: offer Received");
                        startActivityForCall(obj);
                    }
                    else if (json.getString("type").equals("candidate")){
                        Log.d(TAG, "onHandleIntent: candidate Received");
                        //send candidate
                        for (int i=mClients.size()-1; i>=0; i--) {
                            try {
                                Message msg = Message.obtain();
                                msg.what = MSG_SEND_ICE;
                                msg.arg1 = mValue;
                                msg.obj = json;

                                mClients.get(i).send(msg);
                                msg.recycle();
                            } catch (android.os.RemoteException e) {
                                // The client is dead.  Remove it from the list;
                                // we are going through the list from back to front
                                // so this is safe to do inside the loop.
                                mClients.remove(i);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /*while(true){
            *//*ClientWorker w;
            try{
            //server.accept returns a client connection
                w = new ClientWorker(mServerSocket.accept(), this);
                Thread t = new Thread(w);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }*//*




        }*/
    }

    private void startActivityForCall(String s) {
        Log.d("CallActivity", "run: "+s);
        Intent callIntent = new Intent(this , CallActivity.class);
        callIntent.putExtra("json",s);
        //callIntent.putExtra("socket",client)
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        wakeLock.release();
        mNsdManager.unregisterService(mRegistrationListener);
        try {
            mServerSocket.close();
            if (client!=null) client.close();
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
                Toast.makeText(LinkifyIntentService.this, "Service Registered Successfully", Toast.LENGTH_LONG).show();
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

    public class ServiceBinder extends Binder {
        LinkifyIntentService getService() {
            return LinkifyIntentService.this;
        }
    }

    private static class IncomingHandler extends Handler {
        private WeakReference<LinkifyIntentService> mService;

        IncomingHandler(LinkifyIntentService mService) {
            this.mService = new WeakReference<>(mService);
        }

        @Override
        public void handleMessage(Message msg) {
            Messenger activityMessenger = msg.replyTo;
            Log.i(TAG, "Service received message: " + msg);

            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SEND_SDP:
                        LinkifyIntentService service = mService.get();
                        service.handleMsg(msg);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    public void handleMsg(Message msg){
        AppExecutor.getInstance().getNetworkExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "run: sending object");

                    OutputStream out = client.getOutputStream();
                    ObjectOutputStream o = new ObjectOutputStream(out);
                    o.writeObject(msg.obj);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
