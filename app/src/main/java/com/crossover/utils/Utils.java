package com.crossover.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;

import com.crossover.R;

/**
 * Created by Sumit on 11/5/16.
 */

public class Utils {

    /**
     * Check for offline
     * @param context
     * @return
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static void handleOfflineIssue(Context context){
        if(!isConnectedToInternet(context)){
            showGlobalSnackBar(context, context.getString(R.string.connectionOffline));
        }
    }

    /**
     * Show snack bar at application level.
     * @param context
     * @param message
     */
    public static void showGlobalSnackBar(Context context, String message) {

        final GlobalSnackBar snackbarWrapper = GlobalSnackBar.make(context,
                message, Snackbar.LENGTH_LONG);

        snackbarWrapper.show();
    }
}
