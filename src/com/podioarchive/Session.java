package com.podioarchive;

import com.podio.APIFactory;

/**
 * Created by Miguel Prata Leal on 19/07/16.
 */
public class Session {
    private static String archiveURL;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;

    private static APIFactory apiFactory;

    public static APIFactory getApiFactory() {
        return apiFactory;
    }

    public static void setApiFactory(APIFactory apiFactory) {
        Session.apiFactory = apiFactory;
    }

    public static String getArchiveURL() {
        return archiveURL;
    }

    public static void setArchiveURL(String archiveURL) {
        Session.archiveURL = archiveURL;
    }

    public static String getClientId() {
        return CLIENT_ID;
    }

    public static void setClientId(String clientId) {
        CLIENT_ID = clientId;
    }

    public static String getClientSecret() {
        return CLIENT_SECRET;
    }

    public static void setClientSecret(String clientSecret) {
        CLIENT_SECRET = clientSecret;
    }
}
