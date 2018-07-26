package org.busaracenter.es;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.busaracenter.es.model.InputStat;
import org.busaracenter.es.model.PageStat;
import org.busaracenter.es.receiver.NotificationReceiver;
import org.busaracenter.es.session.SessionManager;
import org.busaracenter.es.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.busaracenter.es.service.ESSyncAdapter;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Pattern;

public class ES extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        ESSyncAdapter.initializeSyncAdapter(webView.getContext());
        Utils.clearNotifications(webView.getContext());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("addRecord")) {
            addRecord(args, callbackContext);
            return true;

        } else if (action.equals("getSessionDetails")) {
            getSessionDetails(args, callbackContext);
            return true;

        }

        return super.execute(action, args, callbackContext);
    }

    private void addRecord(JSONArray args, CallbackContext callbackContext) {

        SessionManager session = new SessionManager(webView.getContext());

        try {

            String record = args.getString(0);
            Gson gson = Utils.getGson();
            PageStat stat = gson.fromJson(record, PageStat.class);
            stat.save();

            if (stat.getPageName().endsWith("goal_type_desc")) {
                String goalType = stat.getPageName().replace("_goal_type_desc", "");
                session.setGoalType(goalType);

            } else if (stat.getPageName().endsWith("goal_amount_selection_complete")) {
                String groupType = stat.getPageName().replace("_goal_amount_selection_complete", "");
                session.setGroupType(Utils.getGroupType(groupType));

            }

            Utils.scheduleAlarmManager(webView.getContext());

            if (stat.getIsInputPresent().equalsIgnoreCase("yes")) {
                InputStat inputStat = stat.getInputStats();
                inputStat.setLocalRecordId(stat.getId());
                inputStat.save();

                if (inputStat.getName().equalsIgnoreCase("phone_number")) {
                    session.setPhone(inputStat.getFinalInputValue());

                } else if (inputStat.getName().equalsIgnoreCase("month")) {
                    session.setMonth(inputStat.getFinalInputValue());

                } else if (inputStat.getName().equalsIgnoreCase("contribution_amount")) {
                    String previousContributions = session.getContributions();
                    String allContributions;
                    if (previousContributions.split(",").length == 0)
                        allContributions = previousContributions;
                    else
                        allContributions = previousContributions + "," + inputStat.getFinalInputValue();
                    session.setContributions(allContributions);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            callbackContext.error(ex.getMessage());
        }
    }

    private void getSessionDetails(JSONArray args, CallbackContext callbackContext) {

        Log.e("STATUS", "Getting session details");

        int[] daysInMonths = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        Calendar calendar = Calendar.getInstance();
        int monthDays = daysInMonths[calendar.get(Calendar.MONTH)];
        int remainingDays = monthDays - calendar.get(Calendar.DATE);

        JSONObject json = new JSONObject();
        SessionManager session = new SessionManager(webView.getContext());
        double maxAmount = Utils.getSavingAmountByGroup().get(session.getGroupType());
        String contributions = session.getContributions();
        double totalContributions = 0;
        for (String c: contributions.split(",")) {
            if (Pattern.matches("\\d+(\\.\\d+)?", c))
                totalContributions += Double.parseDouble(c);
        }

        try {
            json.put("phone", session.getPhone());
            json.put("month", session.getMonth());
            json.put("goalType", session.getGoalType());
            json.put("groupType", session.getGroupType());
            json.put("contributions", contributions);
            json.put("isValid", session.isSessionValid());
            json.put("maxAmount", maxAmount);
            json.put("totalContributions", totalContributions);
            json.put("balance", (maxAmount - totalContributions));
            json.put("monthDays", monthDays);
            json.put("remainingDays", remainingDays);

            callbackContext.success(json);

        } catch (Exception ex) {
            ex.printStackTrace();
            callbackContext.error(ex.getMessage());
        }
    }
}