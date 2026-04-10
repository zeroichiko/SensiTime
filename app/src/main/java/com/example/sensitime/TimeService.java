package com.example.sensitime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TimeService extends Service implements TextToSpeech.OnInitListener, SensorEventListener {
    private static final String CHANNEL_ID = "sensitime_channel";
    
    private TextToSpeech tts;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private AudioManager audioManager;
    private PowerManager powerManager;
    private boolean isTtsReady = false;
    private long lastTriggerTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor != null) {
            // Use UI delay to ensure responsiveness during sleep/charge states
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_TEST_VOICE".equals(intent.getAction())) {
            speakTime(); 
            return START_STICKY;
        }

        // CRITICAL: Start as Foreground Service to prevent being killed by Android OS
        startForegroundServiceNow();
        return START_STICKY;  // Always restart service if it crashes/stops unexpectedly
    }

    private void startForegroundServiceNow() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create notification channel for Android O+ 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, 
                "SensiTime Background Service", 
                NotificationManager.IMPORTANCE_DEFAULT  // Default importance to ensure visibility
            );
            manager.createNotificationChannel(channel);
        }

        // Build the notification (user cannot dismiss it while service is running)
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SensiTime - Running")
                .setContentText("Monitoring proximity sensor for time announcements")
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)  // More visible icon
                .setOngoing(true);  // Prevent user from swiping away the notification

        Notification notification = builder.build();

        try {
            startForeground(1, notification);
        } catch (Exception e) {
            // Fallback for cases where foreground service fails on some OS versions
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            isTtsReady = true;
        }
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus == null) return false;

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) return;

        // Condition: Screen MUST be off to avoid accidental noise during active use
        if (powerManager != null && !powerManager.isInteractive()) {
            SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);
            
            // 1. Energy Optimization: Check "Charging Only" condition BEFORE any other logic
            boolean chargingOnly = prefs.getBoolean("charging_only", true);
            if (chargingOnly) {
                if (!isCharging()) {
                    return; // Exit immediately if not charging, preventing CPU wake-up/TTS processing
                }
            }

            // 2. Proximity Trigger Logic
            if (prefs.getBoolean("prox_trigger", true)) {
                float distance = event.values[0];
                int threshold = prefs.getInt("dist", 5);
                if (distance < threshold) {
                    executeSpeakSequence();
                }
            }
        }
    }

    private void executeSpeakSequence() {
        SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);
        long now = System.currentTimeMillis();
        int debounceSec = prefs.getInt("debounce", 5);

        if (now - lastTriggerTime > (debounceSec * 1000L)) {
            speakTime();
            lastTriggerTime = now;
        }
    }

    private void speakTime() {
        if (!isTtsReady) return;

        SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);
        int targetVol = prefs.getInt("vol", 7);
        float speechRate = prefs.getFloat("rate", 1.0f);
        
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0);
        tts.setSpeechRate(speechRate);

        SimpleDateFormat sdf = new SimpleDateFormat("HH mm", Locale.US);
        String timeText = sdf.format(new Date());

        tts.speak(timeText, TextToSpeech.QUEUE_FLUSH, null, "SENSI_SPOKEN_TIME");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}