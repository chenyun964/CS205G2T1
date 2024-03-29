package com.example.serviceexample;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    private String ticker;
    final private String token = "c98k0bqad3ibrc52p340"; // put your own token

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            ticker = msg.getData().getString("ticker");
            // url to get historical data
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

            // parse the json string into 'close' and 'open' array
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

            if (jsonArrayClose == null) {
                Log.v("error", "error occurred");
            } else {
                try {
                    for (int i = 0; i < jsonArrayClose.length(); i++) {
                        double close = jsonArrayClose.getDouble(i);
                        double open = jsonArrayOpen.getDouble(i);
                        String time = jsonArrayTime.getString(i);

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
            intent.putExtra("id", msg.getData().getString("id"));
            intent.putExtra("ticker", ticker);
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
        String ticker = intent.getStringExtra("ticker");
        Intent newIntent = new Intent();
        // Check if ticker data is already in db
        Cursor cursor = getContentResolver().query(HistoricalDataProvider.CONTENT_URI,
                null, "name like '%" + ticker + "%'", new String[]{},
                null);
        // Download the ticker data if not exist
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Download Starting", Toast.LENGTH_SHORT).show();
            Message msg = serviceHandler.obtainMessage();
            Bundle b = new Bundle();
            msg.arg1 = startId;
            b.putString("ticker", ticker);
            b.putString("id",  intent.getStringExtra("id"));
            msg.setData(b);
            serviceHandler.sendMessage(msg);
        } else {
            // Ticker exist, directly move to calculation
            newIntent.setAction("DATA_EXIST");
            newIntent.putExtra("ticker", ticker);
            newIntent.putExtra("id", intent.getStringExtra("id"));
            sendBroadcast(newIntent);
        }
        cursor.close();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Download Done", Toast.LENGTH_SHORT).show();
    }
}
