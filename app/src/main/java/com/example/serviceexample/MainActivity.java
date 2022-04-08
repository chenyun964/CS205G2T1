package com.example.serviceexample;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private Button addBtn;
    private TextView result;
    private EditText ticker;
    private LinearLayout listView;
    private TextView countLabel;
    private BroadcastReceiver myBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<LinearLayout> list = new ArrayList<>();

        // set up layout
        setContentView(R.layout.activitymain);
        listView = (LinearLayout) findViewById(R.id.list_layout);
        addBtn = (Button) findViewById(R.id.add_btn);
        result = (TextView) findViewById(R.id.annual_return);
        ticker = (EditText) findViewById(R.id.ticker_input);
        countLabel = (TextView) findViewById(R.id.counter_label);
        listView.removeAllViews();

        String uri = "@drawable/rounded_ticket";  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());

        // register broadcast receiver to get informed that data is downloaded so that we can calc
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tickerText = ticker.getText().toString();
                Log.v("Ticker Name:", tickerText);
                if (tickerText.matches("")) {
                    Log.v("Input","Ticker input is empty");
                    return;
                }

//                LinearLayout container = new LinearLayout(MainActivity.this);
//                container.setOrientation(LinearLayout.HORIZONTAL);
//
//                LayoutParams params = (LayoutParams) container.getLayoutParams();
//                params.width = 370;
//                params.height = 50;
//                view.setLayoutParams(params);
//                Drawable bg = getResources().getDrawable(imageResource);
//                container.setBackground(bg);

                TextView nTicker = new TextView(MainActivity.this);
                Log.v("txt", tickerText);
                nTicker.setText(tickerText);
                nTicker.setHeight(50);
                nTicker.setWidth(50);
//
//                TextView nReturn = new TextView(MainActivity.this);
//                Log.v("txt", tickerText);
//                nReturn.setText(tickerText);
//                nReturn.setHeight(50);
//                nReturn.setWidth(50);
//
//                TextView nVolat = new TextView(MainActivity.this);
//                Log.v("txt", tickerText);
//                nVolat.setText(tickerText);
//                nVolat.setHeight(50);
//                nVolat.setWidth(50);
//
//                container.addView(nTicker);
//                container.addView(nReturn);
//                container.addView(nVolat);

                listView.addView(nTicker);

                int childCount = listView.getChildCount();
                Log.v("child count:", Integer.toString(childCount));
                countLabel.setText(childCount + " / 5 Ticket Added");
//                Intent intent = new Intent(getApplicationContext(), MyService.class);
//                intent.putExtra("ticker", String.valueOf(ticker.getText()));
//                startService(intent);
//
//                result.setText("Waiting for data.. ");
//                myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
//                registerReceiver(myBroadcastReceiver, new IntentFilter("DOWNLOAD_COMPLETE"));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }


}