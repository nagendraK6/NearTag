package com.neartag.in.services;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neartag.in.MainActivity;
import com.neartag.in.R;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;


import java.util.Random;


/**
 * Created by nagendra on 9/15/18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String channel_id = "com.neartag.in";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("debug_data", "Inside message receive " + remoteMessage.getData().size());
        if (remoteMessage.getData().size() > 0) {
            Log.d("debug_data", "I am here");

            try {

                Log.d("debug_data", "Message data payload: " + remoteMessage.getData());
                JSONObject jsonObject = new JSONObject(remoteMessage.getData());
                String title  = jsonObject.getString("title");
                String message  = jsonObject.getString("message");
                sendNotification(title, message);
                //sendNotification(message);
            } catch (Exception e) {
                Log.d("debug_data", e.getMessage());
                Log.d("debug_data", "hits exception");

            }
        } else {
            sendNotification("Phirki", "Join the game and win real money");
        }



    }

    private void sendNotification(String title, String message) {
        initChannels(this);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel_id);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder nb =
                getIosChannelNotification(title, message);
        mNotificationManager.notify(0, nb.build());

    }

    public NotificationCompat.Builder getIosChannelNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, new Random().nextInt(100), intent,
                PendingIntent.FLAG_ONE_SHOT);
        long when = System.currentTimeMillis();
        //mNotifyBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000});
        //Uri sound_uri  = Uri.parse("uri://" +R.raw.notification);
        //Uri sound_uri = Uri.parse("android.resource://com.relylabs.phirki/notification.mp3");

        return new NotificationCompat.Builder(this, channel_id)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
                .setContentIntent(pendingIntent)
                .setLargeIcon(resize(this,
                        BitmapFactory.decodeResource(
                                getResources(),
                                R.mipmap.neartag_launcher)))
                .setSmallIcon(R.drawable.notification_icon_small)
                .setColor(getResources().getColor(R.color.orange))
                .setAutoCancel(true);
    }

    public static Bitmap resize(Context ctx, Bitmap bm) {

        int width = (int) ctx.getResources().getDimension(android.R.dimen.notification_large_icon_width);
        int height = (int) ctx.getResources().getDimension(android.R.dimen.notification_large_icon_height);
        // String msg = bm.getWidth() + "x" + bm.getHeight() + " --> " + width +
        // "x" + height;
        // RLog.d(NotificationUtil.class, msg);
        //Log.d("debug_data", "is null " + (bm == null));
        return Bitmap.createScaledBitmap(bm, width, height, false);
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channel_id,
                "Phirki Notification",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Receive Phirki Notification");
        notificationManager.createNotificationChannel(channel);

    }
}
