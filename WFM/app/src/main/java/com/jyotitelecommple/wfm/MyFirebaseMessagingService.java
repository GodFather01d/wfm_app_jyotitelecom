package com.jyotitelecommple.wfm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "channel_id";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        if (remoteMessage.getData().size() > 0) {
            // Data payload received
            String alert = remoteMessage.getData().get("alert");
            String tankId = remoteMessage.getData().get("tankid");
            String time = remoteMessage.getData().get("time");
            String city = remoteMessage.getData().get("city");

            sendNotification(alert, tankId, time, city);
        }
    }

    private void sendNotification(String alert, String tankId, String time, String city) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.img)
                .setContentTitle(city + " (" + tankId + ") " + alert)
                .setContentText("Water_Level: " + " " + "%    Tm " + (time))
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
