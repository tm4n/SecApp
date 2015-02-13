package com.tm4n.secapp;

/**
 * Created by Tm4n on 12.02.2015.
 * code adapted from http://www.tutorialspoint.com/android/android_network_connection.htm
 * trust all hosts hack: http://stackoverflow.com/questions/995514/https-connection-android/1000205#1000205
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AsyncChatCon extends AsyncTask<String, Void, String[]> {

    private ChatLog chatlog;
    private TextView inputField;
    private ScrollView scroll;
    private Button buttonSend;
    private Context context;
    public AsyncChatCon(Context context, ChatLog chatlog, TextView inputField, ScrollView scroll, Button buttonSend) {
        this.context = context;
        this.chatlog = chatlog;
        this.inputField = inputField;
        this.scroll = scroll;
        this.buttonSend = buttonSend;
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //check Internet connection.
    private void checkInternetConnection(){
        ConnectivityManager check = (ConnectivityManager) this.context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (check != null)
        {
            /*NetworkInfo[] info = check.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i <info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        Toast.makeText(context, "Internet is connected",
                                Toast.LENGTH_SHORT).show();
                    } */

        }
        else{
            Toast.makeText(context, "Not connected to internet",
                    Toast.LENGTH_SHORT).show();
        }
    }
    protected void onPreExecute(){
        checkInternetConnection();
    }


    @Override
    protected String[] doInBackground(String... arg) {
        try{
            // Disable TLS certificate authentication
            trustAllHosts();

            if (arg[0].equals("refreshall") || arg[0].equals("refresh") || arg[0].equals("users")) {
                // Refresh chat
                String link = arg[1];
                URL url = new URL(link);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setHostnameVerifier(DO_NOT_VERIFY);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (is, "UTF-8"));
                String data = null;
                String webPage = "";
                while ((data = reader.readLine()) != null) {
                    webPage += data + "\n";
                }
                String[] res = new String[2];
                res[0] = arg[0];
                res[1] = webPage;
                return res;
            }

            if (arg[0].equals("send")) {
                // send chat message to server
                String link = arg[1];
                URL url = new URL(link);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setHostnameVerifier(DO_NOT_VERIFY);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // format string to send
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(arg[2]);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (is, "UTF-8"));
                String data = null;
                String webPage = "";
                while ((data = reader.readLine()) != null) {
                    webPage += data + "\n";
                }
                String[] res = new String[2];
                res[0] = arg[0];
                res[1] = webPage;
                return res;
            }


        }catch(Exception e){
            String[] res = new String[2];
            res[0] = "error";
            res[1] = "Exception: " + e.getMessage();
            return res;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] result){

        if (result[0].equals("refresh")) {

            if (result[1].length() > 1) {

                String [] lines = result[1].split("\\n");

                for (String l : lines) {
                    this.chatlog.addMessage(l, true);
                }

                this.chatlog.updateChatTextView();
                this.chatlog.updateUsersTextView();
            }
        }

        if (result[0].equals("refreshall")) {

            if (result[1].length() > 1) {

                String [] lines = result[1].split("\\n");

                for (String l : lines) {
                    this.chatlog.addMessage(l, false);
                }
            }

            this.chatlog.updateChatTextView();
        }


        if (result[0].equals("users")) {

            if (result[1].length() > 1) {

                String [] lines = result[1].split("\\n");

                for (String l : lines) {
                    this.chatlog.addUser(l);
                }
            }

            this.chatlog.updateUsersTextView();
        }


        if (result[0].equals("send")) {
            this.buttonSend.setEnabled(true);
        }


        if (result[0].equals("error")) {
            Toast.makeText(context, "Error: " + result[1],
                    Toast.LENGTH_SHORT).show();
        }
    }
}
