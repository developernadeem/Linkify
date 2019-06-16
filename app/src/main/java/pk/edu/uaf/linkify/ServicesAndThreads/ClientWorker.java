package pk.edu.uaf.linkify.ServicesAndThreads;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import pk.edu.uaf.linkify.Interfaces.StreamMessages;

public class ClientWorker implements Runnable {
    private Socket client;
    private BlockingQueue<String> queue;
    private StreamMessages mCallBack;
    private static final String TAG = "ClientWorker";

    @Override
    protected void finalize() throws Throwable {

        client.close();
        super.finalize();
    }

    //Constructor
    ClientWorker(Socket client, BlockingQueue<String> queue, StreamMessages mCallBack) {
        this.client = client;
        this.queue = queue;
        this.mCallBack = mCallBack;
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

                Log.d(TAG, " Receding from service: " + obj);
                if (obj == null) break;
                mCallBack.onStreamMessage(obj);

            }
        } catch (IOException e) {
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
