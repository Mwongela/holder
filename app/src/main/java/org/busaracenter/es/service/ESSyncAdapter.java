package org.busaracenter.es.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.busaracenter.es.model.Contribution;
import org.busaracenter.es.model.Goal;
import org.busaracenter.es.model.GroupSaving;
import org.busaracenter.es.model.InputStat;
import org.busaracenter.es.model.PageStat;
import org.busaracenter.es.network.API;
import org.busaracenter.es.network.APIClient;
import org.busaracenter.es.request.SaveAnalyticsRequest;
import org.busaracenter.es.request.SaveGoalRequest;
import org.busaracenter.es.request.SaveGroupContributionRequest;
import org.busaracenter.es.session.SessionManager;
import org.busaracenter.es.util.Utils;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import needle.Needle;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ESSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final int SYNC_INTERVAL = 15;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final int NOTIFICATION_ID = 3004;

    public ESSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i("MyServiceSyncAdapter", "onPerformSync");

        SessionManager session = new SessionManager(getContext());

        Utils.scheduleAlarmManager(getContext());

        String currentMonth = Utils.getCurrentMonth();
        String sessionMonth = session.getMonth();

        if (!currentMonth.equals(sessionMonth)) {
            // TODO: Check if app is in foreground
            session.clear();
        }

        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                syncAnalytics(session);
            }
        });

        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                saveGoalsOnline(session);
            }
        });

        Needle.onMainThread().execute(new Runnable() {
            @Override
            public void run() {
                syncGroupSaving(session);
            }
        });
    }

    private void syncAnalytics(SessionManager session) {
        List<PageStat> pageStats = new ArrayList<>();
        Iterator<PageStat> pageStatIterator = PageStat.findAll(PageStat.class);
        while (pageStatIterator.hasNext()) {
            PageStat pageStat = pageStatIterator.next();

            List<InputStat> inputStats = InputStat.find(InputStat.class, "LOCAL_RECORD_ID=?", pageStat.getId().toString());
            if (inputStats.size() > 0) {
                pageStat.setInputStats(inputStats.get(0)); // Assuming that each page has only one input field.
            }

            pageStats.add(pageStat);
        }

        if (session.getPhone().equals("")) return;


        SaveAnalyticsRequest request = new SaveAnalyticsRequest();
        request.setPhoneNumber(session.getPhone());
        request.setDeviceBuild(Build.BRAND + "-" + Build.MODEL + "-" + Build.SERIAL);
        request.setPageStats(pageStats);

        Call<ResponseBody> call = APIClient.getClient(getContext()).create(API.class).postAnalytics(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {

                    int code = response.code();
                    if (code >= 200 && code < 300) {
                        for (PageStat pageStat : pageStats) {
                            if (pageStat.getIsInputPresent().equalsIgnoreCase("yes")) {
                                InputStat inputStat = pageStat.getInputStats();
                                inputStat.delete();
                            }

                            pageStat.delete();
                        }
                    }

                } catch (Exception ex) {

                    onFailure(call, ex.getCause());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void saveGoalsOnline(SessionManager session) {
        List<Goal> goals = new ArrayList<>();
        Iterator<Goal> results = Goal.findAll(Goal.class);
        while (results.hasNext()) {
            Goal goal = results.next();

            // Contributions
            List<Contribution> contributions = Contribution.find(Contribution.class,
                    "GOAL=? AND UPLOADED=?", String.valueOf(goal.getId()), "0");
            goal.setContributions(contributions);

            if (goal.isUploaded() && contributions.size() == 0) {
                continue;
            }

            goals.add(goal);
        }

        SaveGoalRequest request = new SaveGoalRequest();
        request.setPhoneNumber(session.getPhone());
        request.setDeviceBuild(Build.BRAND + "-" + Build.MODEL + "-" + Build.SERIAL);
        request.setGoals(goals);
        Call<ResponseBody> call = APIClient.getClient(getContext()).create(API.class).postGoals(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {

                    int code = response.code();
                    if (code >= 200 && code < 300) {
                        // Success. Update upload status
                        for (Goal goal : goals) {
                            goal.setUploaded(true);
                            goal.save();

                            for (Contribution contribution : goal.getContributions()) {
                                contribution.setUploaded(true);
                                contribution.save();
                            }
                        }
                    }
                } catch (Exception ex) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    public void syncGroupSaving(SessionManager session) {
        // Get Only my records
        // Get IDs of records which I have
        Iterator<GroupSaving> groupSavings = GroupSaving.findAll(GroupSaving.class);
        List<GroupSaving> myRecords = GroupSaving.find(GroupSaving.class, "UPLOADED=?", "0");
        Iterator<Goal> goals = Goal.findAll(Goal.class);


        SaveGroupContributionRequest request = new SaveGroupContributionRequest();

        List<String> recordsInMyPossession = new ArrayList<>();
        while (groupSavings.hasNext()) {
            GroupSaving groupSaving = groupSavings.next();
            recordsInMyPossession.add(groupSaving.getRemoteId());
        }

        List<String> syncGroups = new ArrayList<>();
        while (goals.hasNext()) {
            Goal goal = goals.next();
            String groupGoal = goal.getGroupType() + "," + goal.getGoalType();
            if (syncGroups.contains(groupGoal)) {
                continue;
            }

            syncGroups.add(groupGoal);
        }

        request.setGroupSavingList(myRecords);
        request.setRecordsInMyPossession(recordsInMyPossession);
        request.setSyncGroups(syncGroups);

        Call<ResponseBody> call = APIClient.getClient(getContext()).create(API.class).syncGroupSaving(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    int code = response.code();

                    if (code >= 200 && code < 300) {
                        for (GroupSaving group : myRecords) {
                            group.setUploaded(true);
                            group.save();
                        }

                        JSONObject json = new JSONObject(response.body().string());
                        Gson gson = Utils.getGson();
                        Type listType = new TypeToken<List<GroupSaving>>() {
                        }.getType();
                        List<GroupSaving> newRecords = gson.fromJson(json.getJSONArray("newRecords").toString(), listType);

                        for (GroupSaving s : newRecords) {
                            s.save();
                        }
                    }

                } catch (Exception ex) {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = "org.busaracenter.es.content";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.i("MyServiceSyncAdapter", "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), "org.busaracenter.es.content", bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {

        SessionManager sessionManagement = new SessionManager(context);

        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account("Default"/*sessionManagement.getPhone()*/, "org.busaracenter.es.account");

        if (accountManager.getPassword(newAccount) == null) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.e("MyServiceSyncAdapter", "getSyncAccount Failed to create new account.");
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.i("MyServiceSyncAdapter", "onAccountCreated");
        ESSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, "org.busaracenter.es.content", true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d("MyServiceSyncAdapter", "initializeSyncAdapter");
        getSyncAccount(context);
    }

}