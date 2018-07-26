package org.busaracenter.es.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.busaracenter.es.service.ESSyncAdapter;

public class ESSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static ESSyncAdapter syncServiceAdapter = null;

    @Override
    public void onCreate(){
        Log.d("RecruitmentsSyncService", "onCreate fn()");
        synchronized (sSyncAdapterLock){
            if (syncServiceAdapter == null){
                syncServiceAdapter = new ESSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MyServiceSync", "onBind");
        return syncServiceAdapter.getSyncAdapterBinder();
    }
}