package com.kkeu212.mart212mekarsari.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import com.kkeu212.mart212mekarsari.R;

public class NotificationHelper extends ContextWrapper{

    private static final String MART212_CHANEL_ID = "com.kkeu212.mart212mekarsari.212MartMekarsari";
    private static final String MART212_CHANEL_NAME = "212Mart Mekarsari";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel mart212Channel = new NotificationChannel(MART212_CHANEL_ID,MART212_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        mart212Channel.enableLights(false);
        mart212Channel.enableVibration(true);
        mart212Channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(mart212Channel);
    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder get212MartChannelNotification(String title, String body, PendingIntent contentIntent,
                                                              Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),MART212_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder get212MartChannelNotification(String title, String body,
                                                              Uri soundUri)
    {
        return new Notification.Builder(getApplicationContext(),MART212_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
