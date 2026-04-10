package com.example.sensitime;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    private EditText etDistance, etVolume, etRate, etDebounce;
    private CheckBox cbChargingOnly, cbProxTrigger, cbTouchTrigger, cbBtnTrigger;
    private Button btnStart, btnStop, btnTest;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Build Root Layout (Dark Theme: Black background with white text)
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER);
        rootLayout.setPadding(50, 50, 50, 50);
        rootLayout.setBackgroundColor(Color.BLACK);

        SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);

        // --- Section: Parameter Settings ---
        addSectionTitle(rootLayout, "Parameter Settings");
        etDistance = createLabeledInput(rootLayout, "Proximity Threshold (cm):", prefs.getInt("dist", 5));
        etVolume = createLabeledInput(rootLayout, "Voice Volume (0-15):", prefs.getInt("vol", 7));
        // Speech rate supports decimal values for precision (e.g., 1.2)
        etRate = createDecimalInput(rootLayout, "Speech Rate (0.5 - 2.0):", String.valueOf(prefs.getFloat("rate", 1.0f)));
        etDebounce = createLabeledInput(rootLayout, "Debounce Interval (sec):", prefs.getInt("debounce", 5));

        // --- Section: Trigger Options ---
        addSectionTitle(rootLayout, "Trigger Options");
        cbChargingOnly = createToggle(rootLayout, "Require Charging to Trigger", prefs.getBoolean("charging_only", true));
        cbProxTrigger = createToggle(rootLayout, "Enable Proximity Trigger", prefs.getBoolean("prox_trigger", true));

        // --- Section: Controls ---
        addSectionTitle(rootLayout, "Controls");

        btnStart = new Button(this);
        btnStart.setText("Start Service");
        btnStart.setTextColor(Color.WHITE);
        btnStart.setOnClickListener(v -> {
            savePrefs();
            
            // Request notification permission for Android 13+ (API 33+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            
            Intent intent = new Intent(this, TimeService.class);
            // Use startForegroundService for Android 8.0+ to prevent crashes
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            statusText.setText("Status: Running in background");
        });
        rootLayout.addView(btnStart);

        btnStop = new Button(this);
        btnStop.setText("Stop Service");
        btnStop.setTextColor(Color.WHITE);
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, TimeService.class));
            statusText.setText("Status: Stopped");
        });
        rootLayout.addView(btnStop);

        btnTest = new Button(this);
        btnTest.setText("Test Current Settings");
        btnTest.setTextColor(Color.WHITE);
        btnTest.setOnClickListener(v -> {
            savePrefs(); 
            Intent intent = new Intent(this, TimeService.class);
            intent.setAction("ACTION_TEST_VOICE");
            startService(intent);
        });
        rootLayout.addView(btnTest);

        statusText = new TextView(this);
        statusText.setText("Status: Idle");
        statusText.setTextColor(Color.WHITE);
        statusText.setGravity(Gravity.CENTER);
        rootLayout.addView(statusText);

        // 2. Set the content view (Critical! This renders the UI)
        setContentView(rootLayout);
    }

    private void addSectionTitle(LinearLayout layout, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.GRAY);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, 40, 0, 20); 
        layout.addView(tv);
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
        // TYPE_NUMBER_FLAG_DECIMAL allows decimal input (e.g., 1.5)
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); 
        et.setText(defaultValue);
        et.setTextColor(Color.WHITE);
        layout.addView(et);
        return et;
    }

    private CheckBox createToggle(LinearLayout layout, String text, boolean defaultValue) {
        CheckBox cb = new CheckBox(this);
        cb.setText(text);
        cb.setTextColor(Color.WHITE);
        cb.setChecked(defaultValue);
        layout.addView(cb);
        return cb;
    }

    private void savePrefs() {
        SharedPreferences.Editor editor = getSharedPreferences("sensitime_prefs", MODE_PRIVATE).edit();
        try {
            editor.putInt("dist", Integer.parseInt(etDistance.getText().toString()));
            editor.putInt("vol", Integer.parseInt(etVolume.getText().toString()));
            editor.putFloat("rate", Float.parseFloat(etRate.getText().toString()));
            editor.putInt("debounce", Integer.parseInt(etDebounce.getText().toString()));
            editor.putBoolean("charging_only", cbChargingOnly.isChecked());
            editor.putBoolean("prox_trigger", cbProxTrigger.isChecked());
        } catch (Exception e) {}
        editor.apply();
    }
}