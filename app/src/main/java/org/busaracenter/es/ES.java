package org.busaracenter.es;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.busaracenter.es.model.Goal;
import org.busaracenter.es.model.GroupSaving;
import org.busaracenter.es.model.InputStat;
import org.busaracenter.es.model.PageStat;
import org.busaracenter.es.receiver.NotificationReceiver;
import org.busaracenter.es.session.SessionManager;
import org.busaracenter.es.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.busaracenter.es.service.ESSyncAdapter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ES extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        ESSyncAdapter.initializeSyncAdapter(webView.getContext());
        ESSyncAdapter.syncImmediately(webView.getContext());
        Utils.clearNotifications(webView.getContext());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.e("ARGS", args.toString());

        if (action.equals("addRecord")) {
            addRecord(args, callbackContext);
            return true;

        } else if (action.equals("getSessionDetails")) {
            getSessionDetails(args, callbackContext);
            return true;
        } else if (action.equals("saveGoalType")) {
            saveGoalType(args, callbackContext);
            return true;
        } else if (action.equals("saveGroupType")) {
            saveGroupType(args, callbackContext);
        } else if (action.equals("getGroupStatistics")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject stats = getGroupStatistics();
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callbackContext.success(stats);
                        }
                    });
                }
            });
            return true;
        } else if (action.equals("getGoals")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    JSONArray goals = getGoals(Utils.getCurrentMonth());
                    cordova.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callbackContext.success(goals);
                        }
                    });
                }
            });
            return true;
        }

        else if (action.equals("setCurrentGoalId")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        long currentGoalId = args.getInt(0);
                        setCurrentGoal(currentGoalId);
                        cordova.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callbackContext.success();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callbackContext.error(e.getMessage());
                    }
                }
            });
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
                Log.e("STATUS", "Setting goal type " + goalType);
                session.setGoalType(goalType);

            } else if (stat.getPageName().endsWith("goal_amount_selection_complete")) {
                String groupType = stat.getPageName().replace("_goal_amount_selection_complete", "");
                session.setGroupType(Utils.getGroupType(groupType));

            } else if (stat.getPageName().endsWith("group_stats") && stat.getPreviousPage().endsWith("goal_amount_selection_complete")) {
                Goal goal = new Goal();
                goal.setGroupType(session.getGroupType());
                goal.setGoalType(session.getGoalType());
                goal.setGoalNote(session.getGoalNote());
                goal.setCreatedAt(System.currentTimeMillis());
                goal.setAllowNotification(session.isAllowNotifications());
                goal.setMonth(session.getMonth());

                goal.save();

                setCurrentGoal(goal.getId());
            }

            if (stat.getIsInputPresent().equalsIgnoreCase("yes")) {
                InputStat inputStat = stat.getInputStats();
                inputStat.setLocalRecordId(stat.getId());
                inputStat.save();

                if (inputStat.getName().equalsIgnoreCase("phone_number")) {
                    session.setPhone(inputStat.getFinalInputValue());

                } else if (inputStat.getName().equalsIgnoreCase("month")) {
                    session.setMonth(inputStat.getFinalInputValue());

                } else if (inputStat.getName().equalsIgnoreCase("goal_short_note")) {
                    session.setGoalNote(inputStat.getFinalInputValue());

                } else if (inputStat.getName().equalsIgnoreCase("contribution_amount")) {
                    String previousContributions = session.getContributions();
                    String allContributions;
                    if (previousContributions.split(",").length == 0)
                        allContributions = inputStat.getFinalInputValue();
                    else
                        allContributions = previousContributions + "," + inputStat.getFinalInputValue();
                    session.setContributions(allContributions);

                    try {
                        String finalInput = inputStat.getFinalInputValue();
                        if (Pattern.matches("\\d+(\\.\\d+)?", finalInput)) {
                            double amount = Double.parseDouble(finalInput);
                            App.amount = amount;
                            saveGroupContribution(amount, session);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (inputStat.getName().equalsIgnoreCase("savings-vehicle")) {
                    App.contributionVehicle = inputStat.getFinalInputValue();

                    App.saveContribution();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            callbackContext.error(ex.getMessage());
        }
    }

    private void saveGroupContribution(double amount, SessionManager session) {

        Goal goal = Goal.findById(Goal.class, App.currentGoalId);
        GroupSaving contrib = new GroupSaving();
        contrib.setAmount(amount);
        contrib.setPhoneNumber(session.getPhone());
        contrib.setMonth(goal.getMonth());
        contrib.setGroupType(goal.getGroupType());
        contrib.setGoalType(goal.getGoalType());
        contrib.setUploaded(false);

        contrib.save();
    }

    private void getSessionDetails(JSONArray args, CallbackContext callbackContext) {

        Log.e("STATUS", "Getting session details");

        int[] daysInMonths = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        Calendar calendar = Calendar.getInstance();
        int monthDays = daysInMonths[calendar.get(Calendar.MONTH)];
        int remainingDays = monthDays - calendar.get(Calendar.DATE);

        JSONObject json = new JSONObject();
        SessionManager session = new SessionManager(webView.getContext());
        double maxAmount = 10;
        if (!session.getGroupType().equals("")) {
            maxAmount = Utils.getSavingAmountByGroup().get(session.getGroupType());
        }
        String contributions = session.getContributions();
        double totalContributions = 0;
        for (String c : contributions.split(",")) {
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

            if (App.currentGoalId != -1) {
                Goal goal = Goal.findById(Goal.class, App.currentGoalId);

                totalContributions = goal.getTotalContributions();
                maxAmount = Utils.getSavingAmountByGroup().get(goal.getGroupType());
                double balance = maxAmount - totalContributions;

                json.put("month", goal.getMonth());
                json.put("goalType", goal.getGoalType());
                json.put("groupType", goal.getGroupType());
                json.put("contributions", "");
                json.put("maxAmount", maxAmount);
                json.put("totalContributions", totalContributions);
                json.put("balance", balance >= 0 ? balance : 0);
            }

            callbackContext.success(json);

        } catch (Exception ex) {
            ex.printStackTrace();
            callbackContext.error(ex.getMessage());
        }
    }

    private void saveGoalType(JSONArray args, CallbackContext callbackContext) {
        Log.e("STATUS", "Saving goal Type");
        try {
            String goalType = args.getString(0);
            Log.e("GOAL_TYPE", args.toString());
            SessionManager session = new SessionManager(webView.getContext());
            session.setGoalType(goalType);
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    private void saveGroupType(JSONArray args, CallbackContext callbackContext) {
        Log.e("STATUS", "Saving group Type");
        try {
            String groupType = args.getString(0);
            Log.e("GOAL_TYPE", args.toString());
            SessionManager session = new SessionManager(webView.getContext());
            session.setGroupType(groupType);
            callbackContext.success();
        } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
    }

    private JSONObject getGroupStatistics() {
        JSONObject json = new JSONObject();

        SessionManager session = new SessionManager(cordova.getContext());

        try {

            Goal goal = null;
            if (App.currentGoalId != -1) {
                goal = Goal.findById(Goal.class, App.currentGoalId);
            }

            List<GroupSaving> groupSavings = null;
            double groupAmount = 0;
            if (goal == null) {
               groupSavings = new ArrayList<>();//GroupSaving.find(GroupSaving.class, "MONTH=?", session.getMonth());
            } else {
                Log.e("GOAL_DETAILS", goal.getGroupType() + " " + goal.getGoalType());
                groupSavings = GroupSaving.find(GroupSaving.class, "MONTH=? AND GROUP_TYPE=? AND GOAL_TYPE=?", goal.getMonth(), goal.getGroupType(), goal.getGoalType());
                groupAmount = Utils.getSavingAmountByGroup().get(goal.getGroupType());
            }
            double groupTotal = 0;
            double groupMax = 0;
            double groupRem = 0;
            HashMap<String, Double> groupMembers = new HashMap<String, Double>();
            int goalReachers = 0;

            boolean iveMadeContribution = false;
            for (GroupSaving groupSaving: groupSavings) {

                groupTotal += groupSaving.getAmount();
                if (groupSaving.getPhoneNumber().equalsIgnoreCase(session.getPhone())) {
                    iveMadeContribution = true;
                }

                if (!groupMembers.containsKey(groupSaving.getPhoneNumber()))
                    groupMembers.put(groupSaving.getPhoneNumber(), 0.0);

                double mmAmount = groupMembers.get(groupSaving.getPhoneNumber());
                mmAmount += groupSaving.getAmount();
                groupMembers.put(groupSaving.getPhoneNumber(), mmAmount);
            }

            int groupSize = iveMadeContribution ? groupMembers.keySet().size() : groupMembers.keySet().size() + 1;

            groupMax = groupSize * groupAmount;

            if (groupMax > groupTotal)
                groupRem = groupMax - groupTotal;

            for (String key: groupMembers.keySet()) {
                Double mmAmount = groupMembers.get(key);
                if (mmAmount >= groupAmount) {
                    goalReachers += 1;
                }
            }

            json.put("groupTotal", groupTotal);
            json.put("groupMax", groupMax);
            json.put("groupRem", groupRem);
            json.put("goalReachers", goalReachers);
            json.put("groupSize", groupSize);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return json;
    }

    private JSONArray getGoals(String month) {
        try {
            Gson gson = new Gson();
            List<Goal> goals = Goal.find(Goal.class, "MONTH=?", month);

            JSONArray array = new JSONArray();

            for (Goal goal: goals) {

                JSONObject goalJson = new JSONObject(gson.toJson(goal));
                goalJson.put("goalMax", Utils.getSavingAmountByGroup().get(goal.getGroupType()));
                goalJson.put("contributions", goal.getTotalContributions());

                array.put(goalJson);
            }

            return array;
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void setCurrentGoal(long goalId) {
        App.currentGoalId = goalId;
    }
}