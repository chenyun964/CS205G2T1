package com.example.serviceexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final Handler handler;

    public MyBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(intent.getAction());
        if (intent.getAction().equals("DOWNLOAD_COMPLETE") || intent.getAction().equals("DATA_EXIST")) {
            String ticker = intent.getStringExtra("ticker");
            int result_id = intent.getIntExtra("result", 0);
            int volatility_id = intent.getIntExtra("volatility", 0);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
                    //TextView result = (TextView) ((Activity) context).findViewById(R.id.annual_return);
                    //TextView volatility = (TextView) ((Activity) context).findViewById(R.id.annual_volatility);

                    int count = 0;
                    double totalRet = 0.0;
                    double totalRetSqr = 0.0;

                    Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                            null, "name like '%" + ticker + "%'", new String[]{}, null);
                    if (cursor.moveToFirst()) {
                        double close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                        double open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                        double returns = (close - open) / open;
                        count++;
                        Log.v("data", String.format("close: %.2f open: %.2f", close, open));
                        while (!cursor.isAfterLast()) {
                            //int id = cursor.getColumnIndex("id");
                            close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                            open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                            returns = (close - open) / open;
                            totalRet += returns; // TODO: Tian Hao Help me pls
                            totalRetSqr += returns * returns; // TODO: Tian Hao Help me pls
                            count++;
                            cursor.moveToNext();
                            Log.v("data", String.format("close: %.2f open: %.2f", close, open));
                        }
                    }
                    // Calculation of the annual values
                    double avg = totalRet / (double) count;
                    double annRet = 250 * avg;

                    double var = totalRetSqr / (double) count - avg * avg;
                    double asd = Math.sqrt(250) * Math.sqrt(var);

                    String toRet = String.format("%.2f", annRet * 100.0);
                    Log.v("toRet", toRet);
                    //result.setText(toRet + "%");

                    String toVRet = String.format("%.2f", asd * 100.0);
                    //volatility.setText(toVRet + "%");
                    Log.v("toVRet", toVRet);
                }
            });
        }
    }
}
