package ru.dubrovinalexeyleonidovich.mobileagent;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

public class AppNotification {
    private static final String CHANNEL_ID =
            String.valueOf((int) (Math.random() * 1000 + 1));

    private static final int ONGOING_NOTIFICATION_ID = (int) (Math.random() * 1000 + 1);

    public static void createNotification(Service context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            O.createNotification(context);
        } else {
            PreO.createNotification(context);
        }
    }

    private static class PreO {

        static void createNotification(Service context) {
            // Create Pending Intents.
            PendingIntent piLaunchMainActivity =
                    getLaunchActivityPI(context.getApplicationContext());

            Notification mNotification =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(getNotificationTitle(context))
                            .setContentText(getNotificationContent())
                            .setLargeIcon(getNotificationIcon(context))
                            .setContentIntent(piLaunchMainActivity)
                            .setStyle(new NotificationCompat.BigTextStyle())
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_MIN)
                            .build();

            context.startForeground(
                    ONGOING_NOTIFICATION_ID, mNotification);
        }
    }

    @TargetApi(26)
    private static class O {

        static void createNotification(Service context) {
            String channelId = createChannel(context);
            Notification notification =
                    buildNotification(context, channelId);
            context.startForeground(
                    ONGOING_NOTIFICATION_ID, notification);
        }

        private static Notification buildNotification(
                Service context, String channelId) {
            // Create Pending Intents.
            PendingIntent piLaunchMainActivity =
                    getLaunchActivityPI(context.getApplicationContext());

            return new Notification.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(getNotificationTitle(context))
                    .setContentText(getNotificationContent())
                    .setLargeIcon(getNotificationIcon(context))
                    .setContentIntent(piLaunchMainActivity)
                    .setStyle(new Notification.BigTextStyle())
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MIN)
                    .build();
        }

        @NonNull
        private static String createChannel(Service context) {
            // Create a channel.
            NotificationManager notificationManager =
                    (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence channelName = "Background channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel =
                    new NotificationChannel(
                            CHANNEL_ID, channelName, importance);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);

            notificationManager.createNotificationChannel(
                    notificationChannel);
            return CHANNEL_ID;
        }
    }

    private static Bitmap getNotificationIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    private static String getNotificationContent() {
        return "Приложение запущено";
    }

    private static String getNotificationTitle(Context context) {
        return context.getString(R.string.app_name);
    }

    private static PendingIntent getLaunchActivityPI(Context context) {
        return PendingIntent.getActivity(
                context,
                1,
                new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
