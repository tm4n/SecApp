package com.tm4n.secapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Tilman on 19.02.2015.
 */
public class BootReceiver extends BroadcastReceiver {
    Context _context;
    @Override
    public void onReceive(Context context, Intent rcvintent) {
        _context = context;

        Intent intent = new Intent(context, BackgroundAlarmReceiver.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(_context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)_context.getSystemService(_context.ALARM_SERVICE);

        long scTime = AsyncChatCon.refreshMillis;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 0, scTime, pendingIntent);

        Log.d("SecApp", "Set alarmManager.setRepeating on bootup! Every 20 Seconds!");
    }
}
