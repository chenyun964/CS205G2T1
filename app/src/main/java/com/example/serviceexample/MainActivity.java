package com.example.serviceexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;
import java.util.ArrayList;
import java.util.Locale;

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
                int childCount = listView.getChildCount();
                if (childCount >= 5) {
                    Log.v("Input","Max ticket added");
                    return;
                }

                String tickerText = ticker.getText().toString();
                Log.v("Ticker Name:", tickerText);
                if (tickerText.matches("")) {
                    Log.v("Input","Ticker input is empty");
                    return;
                }

                LinearLayout container = new LinearLayout(MainActivity.this);
                container.setOrientation(LinearLayout.HORIZONTAL);
                LayoutParams params = new LayoutParams(dpTopx(370, view), LayoutParams.WRAP_CONTENT);
                params.setMargins(dpTopx(24, view), dpTopx(10, view), 0, 0);
                container.setLayoutParams(params);
                container.setPadding(dpTopx(15, view), dpTopx(15, view), dpTopx(15, view), dpTopx(15, view));

                Drawable bg = getResources().getDrawable(imageResource);
                container.setBackground(bg);

                TextView nTicker = new TextView(MainActivity.this);
                nTicker.setText(tickerText.toUpperCase(Locale.ROOT));
                nTicker.setTextColor(Color.parseColor("#FFFFFF"));
                nTicker.setLayoutParams(new TableLayout.LayoutParams(dpTopx(60, view), dpTopx(24, view), 1f));

                TextView nReturn = new TextView(MainActivity.this);
                nReturn.setText("Calculating");
                nReturn.setGravity(Gravity.CENTER);
                nReturn.setTextColor(Color.parseColor("#FFFF4444"));
                nReturn.setLayoutParams(new TableLayout.LayoutParams(dpTopx(80, view), dpTopx(24, view), 1f));


                TextView nVolat = new TextView(MainActivity.this);
                nVolat.setText("Calculating");
                nVolat.setGravity(Gravity.CENTER);
                nVolat.setTextColor(Color.parseColor("#FF99CC00"));
                nVolat.setLayoutParams(new TableLayout.LayoutParams(dpTopx(80, view), dpTopx(24, view), 1f));

                container.addView(nTicker);
                container.addView(nReturn);
                container.addView(nVolat);

                listView.addView(container);
                childCount += 1;
                countLabel.setText(childCount + " / 5 Ticket Added");
                ticker.setText("");
                if(childCount >= 5){
                    ticker.clearFocus();
                    hideSoftKeyboard(MainActivity.this, view);
                }

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

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private int dpTopx(int dp, View view){
        float scale = view.getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
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