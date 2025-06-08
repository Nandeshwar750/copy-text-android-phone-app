package com.example.txt_reader_flut;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.txt_reader_flut/text";
    private static final String NOTIFICATION_CHANNEL_ID = "txt_reader_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START_READING = "com.example.txt_reader_flut.START_READING";
    private static final String ACTION_STOP_READING = "com.example.txt_reader_flut.STOP_READING";
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static FlutterEngine flutterEngine;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        registerReceiver(actionReceiver, new IntentFilter(ACTION_START_READING));
        registerReceiver(actionReceiver, new IntentFilter(ACTION_STOP_READING));

        // Request notification permission on Android 13 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        MainActivity.flutterEngine = flutterEngine;

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler((call, result) -> {
                switch (call.method) {
                    case "startReading":
                        startReading();
                        result.success(null);
                        break;
                    case "stopReading":
                        stopReading();
                        result.success(null);
                        break;
                    case "showNotification":
                        showNotification();
                        result.success(null);
                        break;
                    default:
                        result.notImplemented();
                        break;
                }
            });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Text Reader Channel";
            String description = "Channel for text reader notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        Intent startIntent = new Intent(ACTION_START_READING);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(ACTION_STOP_READING);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_custom_icon)  // Use your custom icon
            .setContentTitle("Text Reader")
            .setContentText("Control text reading")
            .addAction(R.drawable.ic_custom_icon, "Start Reading", startPendingIntent)  // Use your custom icon
            .addAction(R.drawable.ic_custom_icon, "Stop Reading", stopPendingIntent)   // Use your custom icon
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private final BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_START_READING:
                        startService();
                        break;
                    case ACTION_STOP_READING:
                        stopService();
                        break;
                }
            }
        }
    };

    private void startService() {
        if (!isAccessibilityServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 1);
        } else {
            Intent intent = new Intent(this, MyAccessibilityService.class);
            startService(intent);
        }
    }

    private void stopService() {
        Intent intent = new Intent(this, MyAccessibilityService.class);
        stopService(intent);
    }

    private boolean isAccessibilityServiceEnabled() {
        // Implement your logic to check if the accessibility service is enabled
        return false;
    }

    private void startReading() {
        startService();
    }

    private void stopReading() {
        stopService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(actionReceiver);
    }

    public static void sendTextToFlutter(String text) {
        if (flutterEngine != null) {
            new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .invokeMethod("sendText", text);
        }
    }
}
