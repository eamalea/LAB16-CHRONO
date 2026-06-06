package com.example.chronolab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ChronometreService extends Service {

    private static final String TAG = "ChronoService";
    private static final String CHANNEL_ID = "chrono_channel";
    private static final int NOTIFICATION_ID = 1001;

    private final IBinder binder = new LocalBinder();
    private int secondes = 0;
    private boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private NotificationManager notificationManager;

    // Interface pour notifier l'activité des changements de temps
    private OnTimeChangeListener listener;

    public interface OnTimeChangeListener {
        void onTimeUpdate(int seconds);
    }

    public void setOnTimeChangeListener(OnTimeChangeListener listener) {
        this.listener = listener;
    }

    public class LocalBinder extends Binder {
        public ChronometreService getService() {
            return ChronometreService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String action = intent != null ? intent.getAction() : null;

        if ("STOP".equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!isRunning) {
            isRunning = true;
            startForeground(NOTIFICATION_ID, buildNotification());
            startTimer();
        }
        return START_STICKY;  // redémarre si le système tue le service
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    secondes++;
                    // Mettre à jour la notification
                    updateNotification();
                    // Notifier l'activité si elle est attachée
                    if (listener != null) {
                        listener.onTimeUpdate(secondes);
                    }
                    // Rappel dans 1 seconde
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chronomètre",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notification persistante du chronomètre");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Chronomètre actif")
                .setContentText("Temps écoulé : " + formatTime(secondes))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)      // non glissable
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification() {
        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Chronomètre actif")
                .setContentText("Temps écoulé : " + formatTime(secondes))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notif);
    }

    private String formatTime(int totalSec) {
        int minutes = totalSec / 60;
        int seconds = totalSec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        listener = null;  // éviter les fuites
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        isRunning = false;
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        stopForeground(true);
        super.onDestroy();
    }
}