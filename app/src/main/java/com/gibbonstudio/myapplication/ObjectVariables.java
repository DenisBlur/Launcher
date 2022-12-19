package com.gibbonstudio.myapplication;

import android.service.notification.StatusBarNotification;

public class ObjectVariables {

    public static final String NOTIFICATION_LIST = "com.gibbonstudio.notifyservice.NOTIFICATION_LIST";
    public static final String NOTIFICATION_POST = "com.gibbonstudio.notifyservice.NOTIFICATION_POST";
    public static final String SHOW_HIDE_APP_DRAWER = "com.gibbonstudio.appdrawer.SHOW_HIDE";

    private static ObjectVariables instance;
    private static StatusBarNotification playerSbn;

    public static int posHeightAppBar = 80;
    public static int posHeightMediaPlayer = 300;

    private ObjectVariables() {
    }

    public StatusBarNotification getPlayerSbn() {
        return ObjectVariables.playerSbn;
    }

    public void setPlayerSbn(StatusBarNotification sbn) {
        if (sbn == null) {
            ObjectVariables.playerSbn = null;
        } else {
            if (sbn.getPackageName().equals("ru.yandex.music")) {
                if (sbn.getNotification().actions != null) {
                    ObjectVariables.playerSbn = sbn;
                }
            }
        }
    }

    public static synchronized ObjectVariables getInstance() {
        if (instance == null) {
            instance = new ObjectVariables();
        }
        return instance;
    }

}
