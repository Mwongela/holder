package org.busaracenter.es.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.busaracenter.es.MainActivity;
import org.busaracenter.es.R;
import org.busaracenter.es.receiver.NotificationReceiver;
import org.busaracenter.es.session.SessionManager;

import java.util.Calendar;
import java.util.HashMap;

public class Utils {

    public static Gson getGson() {

        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public static void showNotification(Context context, int notificationId, String title, String content) {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_icon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setSound(soundUri);

        // Opening the app when the user clicks on the notification.
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

    public static String getCurrentMonth() {
        String[] monthName = {"January", "February",
                "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};

        Calendar cal = Calendar.getInstance();
        return monthName[cal.get(Calendar.MONTH)];
    }

    public static HashMap<String, Double> getSavingAmountByGroup() {
        HashMap<String, Double> amounts = new HashMap<>();

        amounts.put("savvy", 500.0);
        amounts.put("power", 1000.0);
        amounts.put("super", 2000.0);
        amounts.put("champion", 2000.0); // More than 2000. TODO Improve this

        return amounts;
    }

    public static String getGroupType(String input) {

        String[] groupTypes = {"savvy", "power", "super", "champion"};
        for (String type : groupTypes) {
            if (input.contains(type))
                return type;
        }
        return "";
    }

    public static void scheduleAlarmManager(Context context) {
        SessionManager session = new SessionManager(context);
        if (session.isSessionValid()) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 30);

            alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY * 7, alarmIntent);
        }
    }

    public static void clearNotifications(Context context) {

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
    }

    public static int getWeekofMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }
}