package com.example.serviceexample;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class MyService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    private String token = "c8uo5g2ad3ibdduen72g"; // put your own token

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            // url to get historical data
            String ticker = String.valueOf(msg.obj).toUpperCase();
            String stringUrl = "https://finnhub.io/api/v1/stock/candle?symbol=" + ticker
                    + "&resolution=D&from=1625097601&to=1640995199&token=" + token;
            String result;
            String inputLine;

            try {
                // make GET requests
                URL myUrl = new URL(stringUrl);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();

                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                connection.connect();

                // store json string from GET response
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                reader.close();
                streamReader.close();

                result = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
                result = null;
                Thread.currentThread().interrupt();
            }

            // parse the json string into 'close' and 'volume' array

            JSONObject jsonObject = null;
            JSONArray jsonArrayClose = null;
            JSONArray jsonArrayTime = null;
            JSONArray jsonArrayOpen = null;

            try {
                jsonObject = new JSONObject(result);
                jsonArrayClose = jsonObject.getJSONArray("c");
                jsonArrayTime = jsonObject.getJSONArray("t");
                jsonArrayOpen = jsonObject.getJSONArray("o");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(jsonArrayClose == null){
                Log.v("error", "error occurred");
            } else {
                Log.v("close", String.valueOf(jsonArrayClose.length()));
                Log.v("open", String.valueOf(jsonArrayOpen.length()));
                try {
                    for (int i = 0; i < jsonArrayClose.length(); i++) {
                        double close = jsonArrayClose.getDouble(i);
                        double open = jsonArrayOpen.getDouble(i);
                        String time = jsonArrayTime.getString(i);
                        Log.v("data", i + ":, c: " + close + " o: " + open + "t: " + time);

                        ContentValues values = new ContentValues();
                        values.put(HistoricalDataProvider.Name, ticker);
                        values.put(HistoricalDataProvider.CLOSE, close);
                        values.put(HistoricalDataProvider.OPEN, open);
                        values.put(HistoricalDataProvider.TIME, time);
                        getContentResolver().insert(HistoricalDataProvider.CONTENT_URI, values);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // broadcast message that download is complete
            Intent intent = new Intent("DOWNLOAD_COMPLETE");
            //intent.putStringArrayListExtra("test", (ArrayList<String>) test);
            sendBroadcast(intent);
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("Service", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getContentResolver().delete(HistoricalDataProvider.CONTENT_URI, null, null);
        Toast.makeText(this, "download starting", Toast.LENGTH_SHORT).show();

        String[] tickers = intent.getStringArrayExtra("tickers");

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "download done", Toast.LENGTH_SHORT).show();
    }
}
