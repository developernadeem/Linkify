package pk.edu.uaf.linkify.ServicesAndThreads;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import pk.edu.uaf.linkify.ChatActivity;
import pk.edu.uaf.linkify.ChatDB.ChatDataBase;
import pk.edu.uaf.linkify.HomeActivity.MainActivity2;
import pk.edu.uaf.linkify.Interfaces.ServiceCallBacks;
import pk.edu.uaf.linkify.Interfaces.StreamMessages;
import pk.edu.uaf.linkify.Modal.LinkifyChat;
import pk.edu.uaf.linkify.Modal.LinkifyMessage;
import pk.edu.uaf.linkify.Modal.LinkifyUser;
import pk.edu.uaf.linkify.R;
import pk.edu.uaf.linkify.SimpleSdpObserver;
import pk.edu.uaf.linkify.Utils.PrefUtils;
import pk.edu.uaf.linkify.Utils.UtilsFunctions;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.ACTION_START_SERVICE;
import static pk.edu.uaf.linkify.Utils.AppConstant.CHUNK_SIZE;
import static pk.edu.uaf.linkify.Utils.AppConstant.IN_COMING_VIDEO;
import static pk.edu.uaf.linkify.Utils.AppConstant.IN_COMING_VOICE;
import static pk.edu.uaf.linkify.Utils.AppConstant.NOTIFICATION_CHANEL_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.NOTIFICATION_ID;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VIDEO_OK;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VIDEO_REJECT;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VOICE_OK;
import static pk.edu.uaf.linkify.Utils.AppConstant.OUT_GOING_VOICE_REJECT;
import static pk.edu.uaf.linkify.Utils.AppConstant.SHOW_CONNECT_PAGE;
import static pk.edu.uaf.linkify.Utils.AppConstant.USER_SERVICE_NAME;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getName;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.getSurname;
import static pk.edu.uaf.linkify.Utils.UtilsFunctions.saveImageToDisk;

/**
 * @author Muhammad Nadeem
 */

public class LinkifyService extends Service implements StreamMessages {
    private ServiceCallBacks mCallBacks;

    private static final String TAG = "LinkifyService";
    /**
     * Server Socket for all incoming connections
     */
    private ServerSocket mServerSocket;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    /*
     * Listener For NSD service callbacks
     */
    private NsdManager.RegistrationListener mRegistrationListener;
    /**
     * Blocking receiverQueue for initializer
     */
    BlockingQueue<String> receiverQueue = new LinkedBlockingDeque<>(10);
    /**
     * Blocking receiverQueue for initializer
     */
    BlockingQueue<String> senderQueue = new LinkedBlockingDeque<>(10);
    LinkedList<IceCandidate> remoteCandidates = new LinkedList<>();
    private NotificationChannel notificationChannel;
    int incomingFileSize;
    int currentIndexPointer;
    byte[] imageFileBytes;
    boolean receivingFile;
    private PeerConnectionFactory factory;
    private PeerConnection localPeerConnection;
    private DataChannel localDataChannel;
    private LinkifyUser linkifyUser;
    private long mChatId;
    private boolean isInitiator = false;
    private boolean isAnswerReceived = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(SHOW_CONNECT_PAGE, true);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Linkify Background Service")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        assert action != null;
        if (ACTION_START_SERVICE.equals(action)) {
            initializeRegistrationListener();
            registerService(initializeServerSocket());
            startListening();
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

    @Override
    public void onStreamMessage(String message) {

        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            switch (type) {
                case "offer":
                    Log.d(TAG, "onHandleIntent: Offer Received");
                    doAnswer(json.getString("sdp"));
                    //startActivityForCall(obj, opt);
                    break;
                case "candidate":
                    Log.d(TAG, "onHandleIntent: Candidate Received");
                    IceCandidate candidate = new IceCandidate(json.getString("id"), json.getInt("label"), json.getString("candidate"));
                    if (isAnswerReceived) {
                        localPeerConnection.addIceCandidate(candidate);
                    } else remoteCandidates.add(candidate);

                    break;
                case "answer":
                    Log.d(TAG, "onHandleIntent: Answer Received");
                    isAnswerReceived = true;
                    localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, json.getString("sdp")));
                    if (!remoteCandidates.isEmpty()) {
                        for (IceCandidate remoteCandidate : remoteCandidates) {
                            localPeerConnection.addIceCandidate(remoteCandidate);
                        }
                        remoteCandidates.clear();
                    }
                    break;
                case "user":
                    String user = json.getString("user_info");
                    updateChatId(user);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


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
     *
     * @param port is public port on which service will listen
     */
    public void registerService(int port) {
        String name = PrefUtils.getStringPref(this, USER_SERVICE_NAME);
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);


        NsdManager mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        Log.d(TAG, "registerService: Should be registered");
    }

    /**
     * Initializing server socket
     * port 0 mean get any available port
     */
    public int initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            Log.wtf(TAG, "initializeServerSocket: ", e);
            e.printStackTrace();
        }

        // Store the chosen port.
        return mServerSocket.getLocalPort();
    }

    private void startListening() {
        AppExecutor.getInstance().getNetworkExecutor().execute(() -> {
            while (true) {
                try {
                    Socket client = mServerSocket.accept();
                    ClientWorker clientWorker = new ClientWorker(client, receiverQueue, this);
                    AppExecutor.getInstance().getNetworkExecutor().execute(clientWorker);
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
     *
     * @param info contains network information for user
     */
    public void connectToSocket(NsdServiceInfo info, String userInfo) {
        JSONObject object = new JSONObject();
        try {
            object.put("type", "user");
            object.put("user_info", userInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (info != null) {
            updateChatId(info.getServiceName());
            AppExecutor.getInstance().getNetworkExecutor().execute(() -> {
                try {
                    Socket peer = new Socket(info.getHost(), info.getPort());
                    ClientWorker clientWorker = new ClientWorker(peer, senderQueue, this);
                    AppExecutor.getInstance().getNetworkExecutor().execute(clientWorker);
                    senderQueue.put(object.toString());
                    sendOffer();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
    }

    private void initializePeerConnections() {
        localPeerConnection = createPeerConnection(factory);

        localDataChannel = localPeerConnection.createDataChannel("sendDataChannel", new DataChannel.Init());
        localDataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onBufferedAmountChange(long l) {

            }

            @Override
            public void onStateChange() {
                Log.d(TAG, "onStateChange: " + localDataChannel.state().toString());

                if (localDataChannel.state() == DataChannel.State.OPEN) {
                    // TODO:
                } else {

                }

            }

            @Override
            public void onMessage(DataChannel.Buffer buffer) {

            }
        });
    }

    private void sendOffer() {
        isInitiator = true;
        initializePeerConnectionFactory();
        initializePeerConnections();
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        localPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "onCreateSuccess: offer created");
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    Log.d(TAG, "onCreateSuccess: offer Created" + message);
                    senderQueue.put(message.toString());
                } catch (Exception e) {
                    Log.d(TAG, "onCreateSuccess: " + e.getMessage());
                }
            }
        }, sdpMediaConstraints);
    }

    private void doAnswer(String offer) {
        isInitiator = false;
        initializePeerConnectionFactory();
        initializePeerConnections();
        Log.d(TAG, "doAnswer: called");
        localPeerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, offer));
        localPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                localPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                try {


                    JSONObject message = new JSONObject();

                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    //use handler to send answer
                    Log.d(TAG, "onCreateSuccess: sending message via serviceS");
                    receiverQueue.put(message.toString());

                } catch (JSONException e) {
                    Log.d(TAG, "onCreateSuccess: " + e.getMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
//                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                JSONObject message = new JSONObject();

                try {
                    message.put("type", "candidate");
                    message.put("label", iceCandidate.sdpMLineIndex);
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);
                    if (isInitiator) {
                        senderQueue.put(message.toString());
                    } else {
                        receiverQueue.put(message.toString());
                    }
                    Log.d(TAG, "onIceCandidate: sending candidate " + message);

                } catch (Exception e) {
                    Log.d(TAG, "onIceCandidate: " + e.getMessage());
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: ");
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: is local: " + " , state: " + dataChannel.state());
                dataChannel.registerObserver(new DataChannel.Observer() {
                    @Override
                    public void onBufferedAmountChange(long l) {

                    }

                    @Override
                    public void onStateChange() {
                        Log.d(TAG, "onStateChange: remote data channel state: " + dataChannel.state().toString());
                    }

                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        Log.d(TAG, "onMessage: got message");
                        readIncomingMessage(buffer.data);
                    }
                });
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    public void sendMessage(String message) {

        ByteBuffer data = stringToByteBuffer("-s" + message, Charset.defaultCharset());
        localDataChannel.send(new DataChannel.Buffer(data, false));
    }

    public void sendSignal(String message) {

        ByteBuffer data = stringToByteBuffer("-n" + message, Charset.defaultCharset());
        localDataChannel.send(new DataChannel.Buffer(data, false));
    }

    private void readIncomingMessage(ByteBuffer buffer) {
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        if (!receivingFile) {
            String firstMessage = new String(bytes, Charset.defaultCharset());
            String type = firstMessage.substring(0, 2);

            if (type.equals("-i")) {
                incomingFileSize = Integer.parseInt(firstMessage.substring(2));
                imageFileBytes = new byte[incomingFileSize];
                Log.d(TAG, "readIncomingMessage: incoming file size " + incomingFileSize);
                receivingFile = true;
            } else if (type.equals("-s")) {
                //received msg
                if (mCallBacks != null) {
                    mCallBacks.getUserMessage(firstMessage.substring(2), 1);
                } else {
                    showNotification(firstMessage.substring(2), null);
                    AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
                        ChatDataBase.getInstance(this).myDao().insertMessage(new LinkifyMessage(firstMessage.substring(2), new Date(), null, linkifyUser.getId(), mChatId));
                    });
                }
            } else if (type.equals("-n")) {

                String msgType = firstMessage.substring(2);
                switch (msgType) {
                    case IN_COMING_VIDEO:
                        if (mCallBacks != null) {
                            mCallBacks.inComingVideoCall();
                        } else {
                            Intent callIntent = new Intent(this, ChatActivity.class);
                            callIntent.setAction(IN_COMING_VIDEO);
                            callIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(callIntent);
                        }
                        break;
                    case IN_COMING_VOICE:
                        if (mCallBacks != null) {
                            mCallBacks.inComingVoiceCall();
                        } else {
                            Intent callIntent = new Intent(this, ChatActivity.class);
                            callIntent.setAction(IN_COMING_VOICE);
                            callIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(callIntent);
                        }
                        break;
                    case OUT_GOING_VIDEO_OK:
                        break;
                    case OUT_GOING_VOICE_OK:
                        break;
                    case OUT_GOING_VIDEO_REJECT:
                        break;
                    case OUT_GOING_VOICE_REJECT:
                        break;
                }

            }
        } else {
            for (byte b : bytes) {
                imageFileBytes[currentIndexPointer++] = b;
            }
            if (currentIndexPointer == incomingFileSize) {
                Log.d(TAG, "readIncomingMessage: received all bytes");
                Bitmap bmp = BitmapFactory.decodeByteArray(imageFileBytes, 0, imageFileBytes.length);
                Uri uri = saveImageToDisk(this, bmp);
                if (mCallBacks != null) {
                    mCallBacks.getUserMessage(uri.toString(), 0);
                } else {
                    showNotification(null, bmp);
                    AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
                        ChatDataBase.getInstance(this).myDao().insertMessage(new LinkifyMessage(null, new Date(), uri.toString(), linkifyUser.getId(), mChatId));
                    });
                }
                receivingFile = false;
                currentIndexPointer = 0;
            }
        }
    }

    private void showNotification(String firstMessage, Bitmap bmp) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("id", mChatId);
        intent.putExtra("userId", linkifyUser.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);
        if (notificationChannel == null)
            notificationChannel = UtilsFunctions.getChannel(this);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANEL_ID)
                .setContentTitle("Message")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (bmp != null) {
            nb.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bmp)
                    .bigLargeIcon(null));
        } else nb.setContentText(firstMessage);
        manager.notify(1, nb.build());
    }

    private static ByteBuffer stringToByteBuffer(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public void setCallbacks(ServiceCallBacks callbacks) {
        mCallBacks = callbacks;
    }

    private void updateChatId(String userInfo) {

        String[] nameParts = userInfo.split("/");
        String builder = getName(nameParts[0]).substring(0, 1) +
                getSurname(nameParts[0]).substring(0, 1);
        linkifyUser = new LinkifyUser(nameParts[2], nameParts[0], builder, nameParts[1]);

        AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
            int chatId = ChatDataBase.getInstance(this).myDao().searchChatByUserId(linkifyUser.getId());
            if (chatId > 0) {
                mChatId = chatId;
            } else {
                int numberOfRows = ChatDataBase.getInstance(this).myDao().updateUser(linkifyUser);
                if (numberOfRows == 0) {
                    ChatDataBase.getInstance(this).myDao().insertUser(linkifyUser);
                }
                mChatId = ChatDataBase.getInstance(this).myDao().insertChat(new LinkifyChat(linkifyUser.getId(), new Date(), 0, ""));
            }
        });
    }

    public void sendImage(String picturePath) {
        File file = new File(picturePath);
        int size = (int) file.length();
        int numberOfChunks = size / CHUNK_SIZE;
        byte[] bytes = UtilsFunctions.readPickedFileAsBytes(file, size);

        ByteBuffer meta = stringToByteBuffer("-i" + size, Charset.defaultCharset());
        localDataChannel.send(new DataChannel.Buffer(meta, false));

        for (int i = 0; i < numberOfChunks; i++) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, i * CHUNK_SIZE, CHUNK_SIZE);
            localDataChannel.send(new DataChannel.Buffer(wrap, false));
        }
        int remainder = size % CHUNK_SIZE;
        if (remainder > 0) {
            ByteBuffer wrap = ByteBuffer.wrap(bytes, numberOfChunks * CHUNK_SIZE, remainder);
            localDataChannel.send(new DataChannel.Buffer(wrap, false));
        }
    }


}
