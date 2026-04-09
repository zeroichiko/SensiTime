package com.example.sensitime;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    private EditText etDistance, etVolume, etRate, etDebounce;
    private Button btnStart, btnStop, btnTest;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(Color.BLACK);

        SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);

        etDistance = createLabeledInput(layout, "Proximity Threshold (cm):", prefs.getInt("dist", 5));
        etVolume = createLabeledInput(layout, "Voice Volume (0-15):", prefs.getInt("vol", 7));
        etRate = createDecimalInput(layout, "Speech Rate (0.5 - 2.0):", String.valueOf(prefs.getFloat("rate", 1.0f)));
        etDebounce = createLabeledInput(layout, "Debounce Interval (sec):", prefs.getInt("debounce", 5));

        btnStart = new Button(this);
        btnStart.setText("Start Service");
        btnStart.setTextColor(Color.WHITE);
        btnStart.setOnClickListener(v -> {
            savePrefs();
            startService(new Intent(this, TimeService.class));
            statusText.setText("Status: Running in background");
        });
        layout.addView(btnStart);

        btnStop = new Button(this);
        btnStop.setText("Stop Service");
        btnStop.setTextColor(Color.WHITE);
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, TimeService.class));
            statusText.setText("Status: Stopped");
        });
        layout.addView(btnStop);

        btnTest = new Button(this);
        btnTest.setText("Test Current Settings");
        btnTest.setTextColor(Color.WHITE);
        btnTest.setOnClickListener(v -> {
            savePrefs(); 
            Intent intent = new Intent(this, TimeService.class);
            intent.setAction("ACTION_TEST_VOICE");
            startService(intent);
        });
        layout.addView(btnTest);

        statusText = new TextView(this);
        statusText.setText("Status: Idle");
        statusText.setTextColor(Color.WHITE);
        layout.addView(statusText);

        setContentView(layout);
    }

    private EditText createLabeledInput(LinearLayout layout, String labelText, int defaultValue) {
        TextView tv = new TextView(this);
        tv.setText(labelText);
        tv.setTextColor(Color.WHITE);
        layout.addView(tv);

        EditText et = new EditText(this);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(defaultValue));
        et.setTextColor(Color.WHITE);
        layout.addView(et);
        return et;
    }

    private EditText createDecimalInput(LinearLayout layout, String labelText, String defaultValue) {
        TextView tv = new TextView(this);
        tv.setText(labelText);
        tv.setTextColor(Color.WHITE);
        layout.addView(tv);

        EditText et = new EditText(this);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 32); 
        et.setText(defaultValue);
        et.setTextColor(Color.WHITE);
        layout.addView(et);
        return et;
    }

    private void savePrefs() {
        SharedPreferences.Editor editor = getSharedPreferences("sensitime_prefs", MODE_PRIVATE).edit();
        try {
            editor.putInt("dist", Integer.parseInt(etDistance.getText().toString()));
            editor.putInt("vol", Integer.parseInt(etVolume.getText().toString()));
            editor.putFloat("rate", Float.parseFloat(etRate.getText().toString()));
            editor.putInt("debounce", Integer.parseInt(etDebounce.getText().toString()));
        } catch (Exception e) {}
        editor.apply();
    }
}
