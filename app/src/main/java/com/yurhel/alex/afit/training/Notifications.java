package com.yurhel.alex.afit.training;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.yurhel.alex.afit.R;

public class Notifications extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannel channel = new NotificationChannel("AFitNotification", "AFit", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(this, channel.getId());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        Notification notification = builder.setContentTitle(intent.getStringExtra("msg")).setSmallIcon(R.drawable.ic_rv_exercise).build();
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }
}
