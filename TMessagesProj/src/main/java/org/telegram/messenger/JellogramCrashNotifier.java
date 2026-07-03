package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.telegram.ui.LaunchActivity;

public class JellogramCrashNotifier {

    private static final String CHANNEL_ID = "jellogram_crash_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CRASH_DEVELOPER_URL = "https://tg.me/darklord";
    private static boolean shownOnce;

    public static synchronized void showCrashNotification(Context context) {
        if (context == null || shownOnce) {
            return;
        }
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Jellogram crash",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Crash notify");
                nm.createNotificationChannel(channel);
            }

            Intent reportIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CRASH_DEVELOPER_URL));

            Intent openIntent = new Intent(context, LaunchActivity.class);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingOpen = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

            PendingIntent pendingReport = PendingIntent.getActivity(context, 1, reportIntent, PendingIntent.FLAG_IMMUTABLE);

            int iconRes = R.drawable.icon_3_background;
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(iconRes)
                    .setContentTitle("Jellogram crashed")
                    .setContentText("Jellogram crashed, notify developer")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Jellogram crashed, notify developer"))
                    .setContentIntent(pendingReport)
                    .setAutoCancel(true)
                    .build();

            nm.notify(NOTIFICATION_ID, notification);
            shownOnce = true;
            FileLog.d("JellogramCrashNotifier: crash notification shown");
        } catch (Throwable t) {
            FileLog.e("JellogramCrashNotifier: show failed", t);
        }
    }

    public static synchronized void hideCrashNotification(Context context) {
        if (context == null) {
            return;
        }
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(NOTIFICATION_ID);
                shownOnce = false;
                FileLog.d("JellogramCrashNotifier: notification hidden");
            }
        } catch (Throwable t) {
            FileLog.e("JellogramCrashNotifier: hide failed", t);
        }
    }

    private static int getNotificationIconResId(Context context) {
        try {
            int notificationRes = context.getResources().getIdentifier("notification", "drawable", context.getPackageName());
            if (notificationRes != 0) {
                return notificationRes;
            }
        } catch (Throwable ignore) {}
        try {
            int iconRes = context.getResources().getIdentifier("ic_launcher", "drawable", context.getPackageName());
            if (iconRes != 0) {
                return iconRes;
            }
        } catch (Throwable ignore) {}
        return android.R.drawable.stat_notify_error;
    }
}
