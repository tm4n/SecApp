package com.tm4n.secapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Authenticator;
import java.net.PasswordAuthentication;


public class ChatActivity extends ActionBarActivity {

    public TextView chatTextView;
    public TextView usersTextView;
    public EditText inputEditText;
    public ScrollView scroll;
    public Button sendButton;

    private Handler pollHandler = new Handler();

    public ChatLog chatlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        usersTextView = (TextView)findViewById(R.id.usersTextView);
        chatTextView = (TextView)findViewById(R.id.chatTextView);
        inputEditText = (EditText)findViewById(R.id.inputEditText);
        scroll = (ScrollView)findViewById(R.id.chatScrollView);
        sendButton = (Button)findViewById(R.id.sendButton);

        chatlog = new ChatLog(chatTextView, scroll, usersTextView);

        setAlarm(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start polling, give some time for creation
        pollHandler.postDelayed(runnable, 100);

        // disable background polling
        unsetAlarm(this);

        // check if name is set
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String strName = SP.getString("pref_name", "");

        if (strName.length() > 0) {
            // set global HTTP authenticator
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(strName, "iamanapp".toCharArray());

                }
            });

        } else {
            // if no name is given, go to options menu
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            // notify user that name is missing
            Toast.makeText(this, getString(R.string.toast_set_name), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // remove still running polls
        pollHandler.removeCallbacksAndMessages(null);

        // active alarm
        setAlarm(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // open settings menu
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonSendClick(View view) {
        Button b = (Button)findViewById(R.id.sendButton);
        new AsyncChatCon(this,chatlog,inputEditText,scroll,sendButton).execute("send", "https://0x17.de:12489/send/", inputEditText.getText().toString());

        b.setEnabled(false);
        inputEditText.setText("");
    }

    protected void startAsyncRefresh() {

        // check if name is set
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String strName = SP.getString("pref_name", "");

        // get Url
        final String strUrl = SP.getString("pref_url", "https://0x17.de:12489");

        if (strName.length() > 0 && strUrl.length() > 0) {

            // get URL
            String url = strUrl + "/get/";

            if (chatlog.lastTimestampEpoch > 0) {
                // send last received timestamp
                url += chatlog.lastTimestampEpoch + ":" + chatlog.lastTimestampId + "/";
                new AsyncChatCon(this, chatlog, inputEditText, scroll, sendButton).execute("refresh", url);
                //Log.v("SecAPP", "part refresh started");
            } else {
                // no timestamp, get all anew
                chatlog.removeAllMessages();
                new AsyncChatCon(this, chatlog, inputEditText, scroll, sendButton).execute("refresh", url);

                chatlog.removeAllUsers();
                String userurl = strUrl + "/users/";
                new AsyncChatCon(this, chatlog, inputEditText, scroll, sendButton).execute("users", userurl);

                //Log.v("SecAPP", "full refresh started");
            }
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            startAsyncRefresh();

            // do again in 2 seconds
            pollHandler.postDelayed(this, 2000);
        }
    };



    private void setAlarm(Context context) {

        if (chatlog.lastTimestampEpoch > 0) {
            // safe current last time in preferences
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor preferencesEditor = SP.edit();
            preferencesEditor.putLong("last_timestamp", chatlog.lastTimestampEpoch);
            preferencesEditor.putLong("last_timestamp_id", chatlog.lastTimestampId);
            preferencesEditor.commit();
        }

        Intent intent = new Intent(context, BackgroundAlarmReceiver.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long scTime = AsyncChatCon.refreshMillis;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 0, scTime, pendingIntent);

        Log.d("SecApp", "Set alarmManager.setRepeating");

    }

    private void unsetAlarm(Context context) {

        Intent intent = new Intent(context, BackgroundAlarmReceiver.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        Log.d("SecApp", "Stopped alarmManager.setRepeating");
    }

}
