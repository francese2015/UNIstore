package com.unisa.unistore.utilities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Daniele on 20/04/2015.
 */
public class NetworkUtilities {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtilities.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtilities.TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == NetworkUtilities.TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == NetworkUtilities.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static boolean checkConnection(Activity activity) {
        int conn = NetworkUtilities.getConnectivityStatus(activity);
        boolean status = false;
        if (conn == NetworkUtilities.TYPE_WIFI) {
            status = true;
        } else if (conn == NetworkUtilities.TYPE_MOBILE) {
            status = true;
        } else if (conn == NetworkUtilities.TYPE_NOT_CONNECTED) {
            status = false;
        }

        return status;
    }
}
