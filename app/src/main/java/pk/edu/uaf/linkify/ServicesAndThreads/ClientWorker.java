package pk.edu.uaf.linkify.ServicesAndThreads;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import pk.edu.uaf.linkify.CallActivity;
import pk.edu.uaf.linkify.MainActivity;

import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;

public class ClientWorker implements Runnable {
    private Socket client;
    private Context context;
    private static final String TAG = "ClientWorker";

    @Override
    protected void finalize() throws Throwable {
        context = null;
        client.close();
        super.finalize();
    }

    //Constructor
    ClientWorker(Socket client, Context context) {
        this.client = client;
        this.context = context;
    }

    public void run(){
        String line;
        BufferedReader in = null;
        PrintWriter out = null;
        try{
            in = new BufferedReader(new
                    InputStreamReader(client.getInputStream()));
            out = new
                    PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("in or out failed");
            System.exit(-1);
        }
        String s = "";
        while(true){
            try{
                line = in.readLine();
                if (line ==null)break;
                //Send data back to client
                //out.println(line);
                //Append data to text area
                s += line;
            }catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: ");
                break;
            }
        }
        Log.d("CallActivity", "run: "+s);
        showNotification(s);
        Intent callIntent = new Intent(context , CallActivity.class);
        callIntent.putExtra("json",s);
        //callIntent.putExtra("socket",client)
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  void showNotification(String line){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Linkify Background Service")
                .setContentText(line)
                .setSmallIcon(android.R.drawable.btn_star)
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(/* id */(int)System.currentTimeMillis(), notification);
    }

}
