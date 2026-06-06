package com.example.chronolab;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private TextView tvTemps;
    private Button btnStart, btnStop;
    private ChronometreService chronoService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ChronometreService.LocalBinder binder = (ChronometreService.LocalBinder) service;
            chronoService = binder.getService();
            isBound = true;

            // S'abonner aux mises à jour du temps
            chronoService.setOnTimeChangeListener(seconds -> {
                runOnUiThread(() -> updateTimeDisplay(seconds));
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            chronoService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemps = findViewById(R.id.tvTemps);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);

        // Vérifier la permission de notification (Android 13+)
        checkNotificationPermission();

        btnStart.setOnClickListener(v -> startChronometer());
        btnStop.setOnClickListener(v -> stopChronometer());
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void startChronometer() {
        Intent intent = new Intent(this, ChronometreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Toast.makeText(this, "Chronomètre démarré", Toast.LENGTH_SHORT).show();
    }

    private void stopChronometer() {
        Intent intent = new Intent(this, ChronometreService.class);
        intent.setAction("STOP");
        stopService(intent);

        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        tvTemps.setText("00:00");
        Toast.makeText(this, "Chronomètre arrêté", Toast.LENGTH_SHORT).show();
    }

    private void updateTimeDisplay(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        tvTemps.setText(String.format("%02d:%02d", minutes, secs));
    }

    @Override
    protected void onDestroy() {
        if (isBound) {
            unbindService(connection);
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission notification accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Les notifications ne seront pas affichées", Toast.LENGTH_LONG).show();
            }
        }
    }
}