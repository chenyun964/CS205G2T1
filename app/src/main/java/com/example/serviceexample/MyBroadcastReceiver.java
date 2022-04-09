package com.example.serviceexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.widget.TextView;

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

                    TextView AnnualReturn = (TextView) ((Activity) context).findViewById(txt_id);
                    TextView AnnualVolat = (TextView) ((Activity) context).findViewById(txt_id * 10);

                    int count = 0;   // Store the number of days within the interval
                    double totalRet = 0.0;   // Store the total percentage return within the interval
                    double totalRetSqr = 0.0;   // Store the total (percentage return)^2 within the interval

                    Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                            null, "name like '%" + ticker + "%'", new String[]{}, null);

                    if (cursor.moveToFirst()) {
                        while (!cursor.isAfterLast()) {
                            double close = cursor.getDouble(cursor.getColumnIndexOrThrow("close"));
                            double open = cursor.getDouble(cursor.getColumnIndexOrThrow("open"));
                            double returns = (close - open) / open;
                            totalRet += returns;
                            totalRetSqr += returns * returns;
                            count++;
                            cursor.moveToNext();
                        }
                    } else {
                        AnnualReturn.setText("Ticker not exist");
                        AnnualVolat.setText("");
                        return;
                    }

                    // Calculation of the annualized values
                    double avg = totalRet / (double) count;   // Calculate average daily percentage return within the interval
                    double annRet = 250 * avg;   // Calculate annual return, assuming 250 trading days per year

                    // Calculation of the annualized volatility
                    double var = totalRetSqr / (double) count - avg * avg;  // formula: var = sum(ret^2) / count - avg^2
                    double asd = Math.sqrt(250) * Math.sqrt(var);   // calculate annualized volatility, assuming 250 trading days per year

                    String toRet = String.format("%.2f", annRet * 100.0);

                    // Update UI with calculated result
                    if(annRet <= 0){
                        AnnualReturn.setText(toRet + "%");
                        AnnualReturn.setTextColor(Color.parseColor("#FFFF4444"));
                    } else {
                        AnnualReturn.setText("+" + toRet + "%");
                        AnnualReturn.setTextColor(Color.parseColor("#FF99CC00"));
                    }

                    String toVRet = String.format("%.2f", asd * 100.0);
                    AnnualVolat.setText(toVRet + "%");

                }
            });
        }
    }
}
