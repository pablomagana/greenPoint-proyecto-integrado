package com.app.greenpoint;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Conexiones {
    private Context contexto;
    private static Conexiones instance = null;

    public static Conexiones getInstance(Context c) {
        if (instance == null) {
            instance = new Conexiones(c);
        }
        return instance;
    }

    private Conexiones(Context c) {
        this.contexto = c;
    }

    public boolean conectadoWifi() {
        ConnectivityManager connectivity = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean conectadoMovil() {
        ConnectivityManager connectivity = (ConnectivityManager) contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }
}
