package pk.edu.uaf.linkify.BroadCastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            //ContextCompat.startForegroundService(context,new Intent(context,LinkifyIntentService.class));
        }
    }
}
