package com.example.serviceexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final Handler handler;

    public MyBroadcastReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("DOWNLOAD_COMPLETE") || intent.getAction().equals("DATA_EXIST")) {
            String ticker = intent.getStringExtra("ticker");
            String view_id = intent.getStringExtra("id");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");

                    int txt_id = Integer.parseInt(view_id);
                    Log.v("txt_id", String.valueOf(txt_id));
                    TextView AnnualReturn = (TextView) ((Activity) context).findViewById(txt_id);

                    TextView AnnualVolat = (TextView) ((Activity) context).findViewById(txt_id * 10);

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
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getColumnIndex("id");
                            close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                            open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                            returns = (close - open) / open;
                            totalRet += returns;
                            totalRetSqr += returns * returns;
                            count++;
                            cursor.moveToNext();
                        }
                    }
                    // Calculation of the annual values
                    double avg = totalRet / (double) count;
                    double annRet = 250 * avg;

                    double var = totalRetSqr / (double) count - avg * avg;
                    double asd = Math.sqrt(250) * Math.sqrt(var);

                    String toRet = String.format("%.2f", annRet * 100.0);
                    Log.v("toRet", toRet);
                    AnnualReturn.setText(toRet + "%");

                    String toVRet = String.format("%.2f", asd * 100.0);
                    AnnualVolat.setText(toVRet + "%");
                    Log.v("toVRet", toVRet);
                }
            });
        }
    }
}
