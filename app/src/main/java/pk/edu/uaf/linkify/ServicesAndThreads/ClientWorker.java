package pk.edu.uaf.linkify.ServicesAndThreads;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ClientWorker implements Runnable {
    private Socket client;
    private BlockingQueue<String> queue;
    private static final String TAG = "ClientWorker";

    @Override
    protected void finalize() throws Throwable {

        client.close();
        super.finalize();
    }

    //Constructor
    ClientWorker(Socket client, BlockingQueue<String> queue) {
        this.client = client;
        this.queue = queue;
    }

    public void run() {

        new Thread(() -> {
            DataOutputStream objectOutputStream;
            try {
                objectOutputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String msg = queue.take();
                        objectOutputStream.writeUTF(msg);
                        objectOutputStream.flush();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                objectOutputStream.close();
                objectOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        DataInputStream in = null;
        try {
            in = new DataInputStream(new
                    BufferedInputStream(client.getInputStream()));

            while (true) {
                String obj = in.readUTF();
                int opt = in.readInt();
                Log.d("ffffffff", " Receding from service: " + obj);
                if (obj == null) break;

                JSONObject json = new JSONObject(obj);
                if (json.getString("type").equals("offer")) {
                    Log.d(TAG, "onHandleIntent: offer Received");
                    //startActivityForCall(obj, opt);
                } else if (json.getString("type").equals("candidate")) {
                    Log.d(TAG, "onHandleIntent: candidate Received");
                    //send candidate
                    /*if (serviceCallbacks != null) {
                        serviceCallbacks.getMessageFromService(json);
                    } else {
                        messageQueue.add(json);
                    }*/

                }

            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        try {
            assert in != null;
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
