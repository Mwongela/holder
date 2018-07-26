package org.busaracenter.es.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private ESAuthenticator eSAuthenticator;

    @Override
    public void onCreate(){
        Log.d("MyAuthenticatorService", "onCreate");
        // Create a new authenticator object
        eSAuthenticator = new ESAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MyAuthenticatorService", "onBind");
        return eSAuthenticator.getIBinder();
    }

}