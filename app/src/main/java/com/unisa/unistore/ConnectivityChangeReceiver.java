package com.unisa.unistore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unisa.unistore.utilities.NetworkUtilities;

/**
 * Created by Daniele on 06/07/2015.
 */
public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private final static String TAG = "ConnectivityChangeReceiver";
    private HomeFragment homeFragment;
    private boolean unregistered = false;

    public ConnectivityChangeReceiver(){
        Log.i(TAG,"INIT Receiver");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "OnReceiver");

        int status = NetworkUtilities.getConnectivityStatus(context);

        if (status == NetworkUtilities.TYPE_MOBILE || status == NetworkUtilities.TYPE_WIFI) {
            HomeFragment inst = HomeFragment.instance();
            if(inst != null)  {
                Log.d(TAG, "Inizio a scaricare gli annunci");
                inst.downloadAnnunci(false);
            } else
                Log.d(TAG, "Si è verificato un errore: inst è null");

        }

        Log.v(TAG, "action: " + intent.getAction());
        Log.v(TAG, "component: " + intent.getComponent());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.v(TAG, "key [" + key + "]: " + extras.get(key));
            }
        } else {
            Log.v(TAG, "no extras");
        }
    }

    public boolean isUnregistered() {
        return unregistered;
    }

    public void setUnregistered(boolean unregistered) {
        this.unregistered = unregistered;
    }
}