package org.busaracenter.es.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCas;
import android.util.Log;

import org.busaracenter.es.R;
import org.busaracenter.es.session.SessionManager;
import org.busaracenter.es.util.Utils;

import java.util.regex.Pattern;

import okhttp3.internal.Util;

public class NotificationReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 2000;
    public static final int TOTAL_WEEKS = 4;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("STATUS", "Setting up Lotification");

        // Aha!!!
        SessionManager session = new SessionManager(context);

        String groupType = session.getGroupType();
        double goalAmount = Utils.getSavingAmountByGroup().get(groupType);

        String contributions = session.getContributions();
        double totalContributions = 0;
        double balance = 0;
        for (String c: contributions.split(",")) {
            if (Pattern.matches("\\d+\\.\\d+", c))
                totalContributions += Double.parseDouble(c);
        }

        balance = goalAmount - totalContributions;


        String[] items = {"first", "second", "third", "fourth", "fifth"};
        int week = Utils.getWeekofMonth();

        double installment = balance;

        if ((week - TOTAL_WEEKS) != 0) {
            installment = balance / (double) Math.abs(week - TOTAL_WEEKS);
        }

        String template = String.format(context.getString(R.string.notification_template), goalAmount, items[week - 1], installment);


        Utils.showNotification(context, NOTIFICATION_ID, context.getString(R.string.app_name), template);
    }
}
