package pk.edu.uaf.linkify.ServicesAndThreads;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import pk.edu.uaf.linkify.MainActivity;

import static pk.edu.uaf.linkify.BroadCastReceivers.App.CHANNEL_ID;

class ClientWorker implements Runnable {
    private Socket client;
    private Context context;

    @Override
    protected void finalize() throws Throwable {
        context = null;
        super.finalize();
    }

    //Constructor
    ClientWorker(Socket client, Context textArea) {
        this.client = client;
        this.context = textArea;
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

        while(true){
            try{
                line = in.readLine();
                //Send data back to client
                out.println(line);
                //Append data to text area
                showNotification(line);
            }catch (IOException e) {
                System.out.println("Read failed");
                System.exit(-1);
            }
        }
    }
    private synchronized void showNotification(String line){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Linkify Background Service")
                .setContentText(line)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(/* id */(int)System.currentTimeMillis(), notification);
    }

}
