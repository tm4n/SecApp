package com.tm4n.secapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Created by tm4n on 13/02/15.
 */

public class ChatLog {

    public static class Entry {

        public enum EnumType {
            Msg, Login, Logout, DirectMsg, System
        }

        public EnumType type;
        public long timestamp;
        public int timestamp_id;
        public String sender;
        public String content;

        public Entry(EnumType type, long epoch, int time_id, String sender, String content) {
            this.type = type;
            this.timestamp = epoch;
            this.timestamp_id = time_id;
            this.sender = sender;
            this.content = content;
        }

    }

    public long lastTimestampEpoch = 0L;
    public int lastTimestampId = 0;


    protected TextView txt;
    protected TextView userstxt;
    protected ScrollView scroll;

    // List that stores all the chat entries
    protected Vector<Entry> c;
    // Set that stores currently logged on users
    protected Set<String> u;


    public ChatLog(TextView txt, ScrollView scroll, TextView userstxt) {
        c = new Vector<Entry>();
        u = new HashSet<String>();

        this.txt = txt;
        this.scroll = scroll;
        this.userstxt = userstxt;
    }

    public void addUser(String s) {
        u.add(s);
    }

    public void removeUser(String s) {
        u.remove(s);
    }

    public void removeAllUsers() {
        u.clear();
    }

    public void removeAllMessages() {
        c.removeAllElements();
    }

    public void addMessage(String raw, boolean parseUsers) {
        Entry e = decodeMessage(raw);

        if (e != null) {
            // safe newest received message
            if (e.timestamp > lastTimestampEpoch || (e.timestamp == lastTimestampEpoch && e.timestamp_id > lastTimestampId)) {
                lastTimestampEpoch = e.timestamp;
                lastTimestampId = e.timestamp_id;
            }

            if (parseUsers) {
                // add or remove user
                if (e.type == Entry.EnumType.Login) {
                    u.add(e.sender);
                }
                if (e.type == Entry.EnumType.Logout) {
                    u.remove(e.sender);
                }
            }

            // add no duplicates
            //if (e.timestamp != lastTimestampEpoch || e.timestamp_id != lastTimestampId) c.add(e);
            c.add(e);
        } else {
            e = new Entry(Entry.EnumType.System, 0, 0, "System: ", "Error decoding Message: " + raw);
            c.add(e);
        }
    }

    public static Entry decodeMessage(String raw) {
        try {
            // decompose strings
            String[] main = raw.split("\\|", 2);

            String header = main[0];
            String body = main[1];

            String[] msg = body.split(Pattern.quote(":"), 2);

            String[] metadata = header.split(Pattern.quote(":"));

            // decode meta data

            Entry.EnumType type = Entry.EnumType.Msg;
            //if (metadata[0].equals("m")) type = 1;
            if (metadata[0].equals("+")) type = Entry.EnumType.Login;
            if (metadata[0].equals("-")) type = Entry.EnumType.Logout;
            if (metadata[0].equals("w")) type = Entry.EnumType.DirectMsg;
            if (metadata[0].equals("s")) type = Entry.EnumType.System;

            long epoch = Long.parseLong(metadata[1]);
            int id = Integer.parseInt(metadata[2]);

            return new Entry(type, epoch, id, msg[0], msg[1]);

        } catch (Exception e) {
            return null;
        }
    }

    public void updateChatTextView()
    {
        String s = "";

        for (Entry e : c) {

            Date d = new Date(e.timestamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat("H:mm");
            String time = format.format(d);

            if (e.type == Entry.EnumType.Login || e.type == Entry.EnumType.Logout) {
                s += time + " - " + e.content + "\n";
            } else {
                s += time + " - " + e.sender + ": " + e.content + "\n";
            }

        }

        // update the text view and mark for rescale
        txt.setText(s);
        txt.invalidate();
        txt.requestLayout();


        // scroll to bottom
        scroll.post(new Runnable() {
            @Override
            public void run() {

                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void updateUsersTextView() {
        String s = "Users: ";

        for (String us : u) {
            s += us + ", ";
        }
        s = s.substring(0, s.length()-2);

        userstxt.setText(s);
        userstxt.invalidate();
        userstxt.requestLayout();
    }

}
