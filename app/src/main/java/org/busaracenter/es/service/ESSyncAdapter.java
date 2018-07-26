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

import org.busaracenter.es.model.InputStat;
import org.busaracenter.es.model.PageStat;
import org.busaracenter.es.network.API;
import org.busaracenter.es.network.APIClient;
import org.busaracenter.es.session.SessionManager;
import org.busaracenter.es.util.Utils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        Utils.scheduleAlarmManager(getContext());

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

        SessionManager session = new SessionManager(getContext());

        if (session.getPhone().equals("")) return;

        JSONObject params = new JSONObject();

        try {
            Gson gson = Utils.getGson();

            params.put("analytics", gson.toJson(pageStats));
            params.put("application", "npower");
            params.put("user", session.getPhone());

            Log.e("PARAMS", params.toString());

            Call<ResponseBody> call = APIClient.getClient(getContext()).create(API.class).postData(params.toString());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {

                        String body = response.body().string();

                        if (body.equals("")) {
                            // Success
                            for (PageStat stat: pageStats) {

                                if (stat.getInputStats() != null) {
                                    stat.getInputStats().delete();
                                }

                                stat.delete();
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


        } catch (Exception ex) { ex.printStackTrace(); }
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