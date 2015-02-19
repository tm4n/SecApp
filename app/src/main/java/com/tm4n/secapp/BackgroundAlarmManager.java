package com.tm4n.secapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Tilman on 18.02.2015.
 */

public class BackgroundAlarmManager extends BroadcastReceiver {

    Context _context;
    @Override
    public void onReceive(Context context, Intent intent) {
        _context= context;
        //connect to server..

        Log.d("SecApp", "background alarm started");

    }

}
