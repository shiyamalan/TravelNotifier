package com.example.shiyam.myapplication.network.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.shiyam.myapplication.models.StaticData;

/**
 * Created by shiyam on 1/15/16.
 */
public class ConnectionDetector {

    private static Context _context;

    public ConnectionDetector(Context context){

        this._context = context;
    }

    /**
     * Checking for all possible internet providers
     * **/
    public static boolean isConnectingToInternet(){
        _context = StaticData.current_context;
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
}
