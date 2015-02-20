package com.tm4n.secapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;

/**
 * Created by Tilman on 18.02.2015.
 */

public class BackgroundAlarmReceiver extends BroadcastReceiver {

    Context _context;
    @Override
    public void onReceive(Context context, Intent intent) {
        _context= context;

        Log.d("SecApp", "background alarm started");

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(_context);
        final long timestampEpoch = SP.getLong("last_timestamp", 0);
        final long timestampId = SP.getLong("last_timestamp_id", 0);
        final String strName = SP.getString("pref_name", "");
        final String strUrl = SP.getString("pref_url", "https://0x17.de:12489");
        final boolean enableNoti = SP.getBoolean("pref_enable_noti", true);

        // dont continue if notifications are disabled. This is only for safety, the alarm should not be scheduled if disabled
        if (!enableNoti) return;

        if (timestampEpoch > 0) {
            // we have a timestamp, go check if name is set
            if (strName.length() > 0) {
                // set global HTTP authenticator
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(strName, "iamanapp".toCharArray());

                    }
                });

                // contact server
                String url = strUrl + "/get/" + timestampEpoch + ":" + timestampId + "/";
                new AsyncChatCon(_context, null, null, null, null).execute("refreshbg", url);

            } else Log.e("SecApp", "Empty name in background");

        } else Log.e("SecApp", "Empty timestamp in background");

    }

}
