package com.example.bill.assignmentiot;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Notify extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel H",
                    NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel for Humidity Notification");

//            NotificationChannel channel2 = new NotificationChannel(
//                    CHANNEL_2_ID,
//                    "Channel 2",
//                    NotificationManager.IMPORTANCE_LOW);
//            channel2.setDescription("This is Channel 2");

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
//            manager.createNotificationChannel(channel2);
        }
    }
}