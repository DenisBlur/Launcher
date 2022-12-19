package com.gibbonstudio.myapplication;

import static com.gibbonstudio.myapplication.MainActivity.objectVariables;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_LIST;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_POST;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NLService extends NotificationListenerService {

    private NLServiceReceiver nlservicereciver;
    public static boolean isWork = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //retrieving data from the received intent
        if (intent != null) {
            if (intent.hasExtra("command")) {
                Log.i("NLService", "Started for command '" + intent.getStringExtra("command"));
            } else if (intent.hasExtra("id")) {
                int id = intent.getIntExtra("id", 0);
                String message = intent.getStringExtra("msg");
                Log.i("NLService", "Requested to start explicitly - id : " + id + " message : " + message);
            }
            super.onStartCommand(intent, flags, startId);
        }
        // NOTE: We return STICKY to prevent the automatic service termination
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_LIST);
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        isWork = false;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isWork = true;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(sbn != null) {
            objectVariables.setPlayerSbn(sbn);
            Intent i = new Intent(NOTIFICATION_POST);
            i.putExtra("player", "YANDEX_POST");
            sendBroadcast(i);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(sbn.getPackageName().equals("ru.yandex.music")) {
            objectVariables.setPlayerSbn(null);
            Intent i = new Intent(NOTIFICATION_POST);
            i.putExtra("player", "YANDEX_CLEAR");
            sendBroadcast(i);
        }
    }


    class NLServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getStringExtra("command").equals("list")) {
                    for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                        if (sbn.getPackageName().equals("ru.yandex.music")) {
                            objectVariables.setPlayerSbn(sbn);
                            Intent i = new Intent(NOTIFICATION_POST);
                            i.putExtra("player", "YANDEX_POST");
                            sendBroadcast(i);
                        }
                    }
                }
            }
        }
    }
}