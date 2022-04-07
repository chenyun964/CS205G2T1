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
        if (intent.getAction().equals("DOWNLOAD_COMPLETE")) {
            String ticker = intent.getStringExtra("ticker");
            int result_id = intent.getIntExtra("result", 0);
            int volatility_id  = intent.getIntExtra("volatility", 0 );
            handler.post(new Runnable() {
                @Override
                public void run() {

                    Uri CONTENT_URI = Uri.parse("content://com.example.serviceexample.HistoricalDataProvider/history");
                    TextView result = (TextView) ((Activity) context).findViewById(R.id.annual_return);
                    TextView volatility = (TextView) ((Activity) context).findViewById(R.id.annual_volatility);
                    result.setText("Calculating...");

                    List<Double> sumOpenCloseList = new ArrayList<>();

                    int count = 0;
                    double totalRet = 0.0;
                    double totalRetSqr = 0.0;

                    Cursor cursor = context.getContentResolver().query(CONTENT_URI, null, "name like '%" + ticker +"%'", new String[]{ticker}, null);
                    if (cursor.moveToFirst()) {
                        double returns = cursor.getDouble(cursor.getColumnIndexOrThrow("returns"));
                        totalRet += returns;
                        totalRetSqr += returns * returns;
                        count++;
                        while (!cursor.isAfterLast()) {
                            int id = cursor.getColumnIndex("id");
                            returns = cursor.getDouble(cursor.getColumnIndexOrThrow("returns"));
                            totalRet += returns;
                            totalRetSqr += returns * returns;
                            count ++;
                            cursor.moveToNext();
                            Log.v("data", returns + "");
                        }
                    } else {
                        result.setText("No Records Found");
                    }

                    // Calculation of the annual values
                    double c = (double)(count - 1);
                    double avg = totalRet / c;
                    double annRet = Math.sqrt(250) * avg;

                    double var = totalRetSqr / c - avg * avg;
                    double asd = Math.sqrt(250) * Math.sqrt(var);

                    String toRet = String.format("%.2f", annRet*100.0);
                    result.setText(toRet + "%");

                    String toVRet = String.format("%.2f", asd*100.0);
                    volatility.setText(toVRet + "%");
                    Log.v("Download","Success");
                }
            });
        }
    }
}
