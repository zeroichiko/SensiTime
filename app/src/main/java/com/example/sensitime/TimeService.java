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
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ACTION_TEST_VOICE".equals(intent.getAction())) {
            speakTime(); 
            return START_STICKY;
        }

        startForegroundService();
        return START_STICKY;
    }

    private void startForegroundService() {
        String channelId = "sensitime_channel";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "SensiTime Background Service", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("SensiTime")
                .setContentText("Monitoring proximity while charging...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        try {
            startForeground(1, notification);
        } catch (Exception e) {
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

        if (powerManager != null && !powerManager.isInteractive()) {
            if (isCharging()) {
                float distance = event.values[0];
                SharedPreferences prefs = getSharedPreferences("sensitime_prefs", MODE_PRIVATE);
                int threshold = prefs.getInt("dist", 5);

                if (distance < threshold) {
                    long now = System.currentTimeMillis();
                    int debounceSec = prefs.getInt("debounce", 5);
                    if (now - lastTriggerTime > (debounceSec * 1000L)) { 
                        speakTime();
                        lastTriggerTime = now;
                    }
                }
            }
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
