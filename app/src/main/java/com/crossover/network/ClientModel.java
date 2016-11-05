package com.crossover.network;

import android.content.Context;

/**
 * Created by Sumit on 11/5/16.
 * Helper class to get required network clients.
 */

public class ClientModel {

    /**
     * Return Unauthenticated Client
     * @param context
     * @return
     */
    public static NetworkInterface getUnauthenticatedClient(Context context){
        return ServiceGenerator.createAuthService(NetworkInterface.class);
    }

    /**
     * Return authenticated client
     * @param context
     * @param accessToken
     * @return
     */
    public static NetworkInterface getAuthenticatedClient(Context context, String accessToken){
        return ServiceGenerator.createService(NetworkInterface.class,accessToken);
    }

}
