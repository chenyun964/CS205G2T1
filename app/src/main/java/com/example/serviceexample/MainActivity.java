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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button addBtn;
    private EditText ticker;
    private LinearLayout listView;
    private TextView countLabel;
    private BroadcastReceiver myBroadcastReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initial a broadcast receiver with actions
        myBroadcastReceiver = new MyBroadcastReceiver(new Handler(Looper.getMainLooper()));
        IntentFilter i = new IntentFilter();
        i.addAction("DOWNLOAD_COMPLETE");
        i.addAction("DATA_EXIST");
        registerReceiver(myBroadcastReceiver, i);

        // Set up layout
        setContentView(R.layout.activitymain);
        listView = (LinearLayout) findViewById(R.id.list_layout);
        addBtn = (Button) findViewById(R.id.add_btn);
        ticker = (EditText) findViewById(R.id.ticker_input);
        countLabel = (TextView) findViewById(R.id.counter_label);
        listView.removeAllViews();

        String uri = "@drawable/rounded_ticket";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());

        // On "add" button click
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Input validations
                int childCount = listView.getChildCount() + 1;
                if (childCount > 5) {
                    Toast.makeText(MainActivity.this, "Only 5 ticker is allow", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tickerText = ticker.getText().toString().toUpperCase();
                if (tickerText.matches("")) {
                    Toast.makeText(MainActivity.this, "Ticker input is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create container for text view
                LinearLayout container = new LinearLayout(MainActivity.this);
                container.setOrientation(LinearLayout.HORIZONTAL);
                LayoutParams params = new LayoutParams(dpTopx(370, view), LayoutParams.WRAP_CONTENT);
                params.setMargins(dpTopx(24, view), dpTopx(10, view), 0, 0);
                container.setLayoutParams(params);
                container.setPadding(dpTopx(15, view), dpTopx(15, view), dpTopx(15, view), dpTopx(15, view));
                Drawable bg = getResources().getDrawable(imageResource);
                container.setBackground(bg);

                // Create text view for ticker
                TextView nTicker = new TextView(MainActivity.this);
                nTicker.setText(tickerText.toUpperCase());
                nTicker.setTextColor(Color.parseColor("#FFFFFF"));
                nTicker.setLayoutParams(new TableLayout.LayoutParams(dpTopx(60, view), dpTopx(24, view), 1f));

                // Create text view for annual return
                TextView nReturn = new TextView(MainActivity.this);
                nReturn.setText("Downloading");
                nReturn.setGravity(Gravity.CENTER);
                nReturn.setId(childCount);
                nReturn.setTextColor(Color.parseColor("#FFFF4444"));
                nReturn.setLayoutParams(new TableLayout.LayoutParams(dpTopx(80, view), dpTopx(24, view), 1f));

                // Create text view for annual volatility
                TextView nVolat = new TextView(MainActivity.this);
                nVolat.setText("Downloading");
                nVolat.setGravity(Gravity.CENTER);
                nVolat.setId(childCount * 10);
                nVolat.setTextColor(Color.parseColor("#FFFFFF"));
                nVolat.setLayoutParams(new TableLayout.LayoutParams(dpTopx(80, view), dpTopx(24, view), 1f));

                // Add created element into container
                container.addView(nTicker);
                container.addView(nReturn);
                container.addView(nVolat);

                // Update UI
                listView.addView(container);
                countLabel.setText(childCount + " / 5 Ticker Added");
                ticker.setText("");
                ticker.clearFocus();
                hideSoftKeyboard(MainActivity.this, view);

                // Invoke Service with ticker
                Intent intent = new Intent(getApplicationContext(), MyService.class);
                intent.putExtra("ticker", tickerText);
                intent.putExtra("id", String.valueOf(childCount));
                startService(intent);
            }
        });
    }

    // Hide the keyboard
    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    // Convert dp to px
    private int dpTopx(int dp, View view) {
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