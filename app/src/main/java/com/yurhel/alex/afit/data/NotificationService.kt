package com.yurhel.alex.afit.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.ui.screen_training.TrainingStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NotificationService : Service() {
    companion object {
        var intentSound: Intent? = null

        fun start(context: Context) {
            if (intentSound == null) {
                intentSound = Intent(context, NotificationService::class.java)
                try {
                    context.startForegroundService(intentSound)
                } catch (_: Exception) {}
            }
        }
        fun stop(context: Context) {
            context.stopService(intentSound)
            intentSound = null
        }
    }

    private fun sendBroadcastCustom(
        counter: Int,
        restEnd: Boolean
    ) {
        val intent = Intent("NOTIFICATION").apply {
            putExtra("COUNTER", counter)
            putExtra("REST_END", restEnd)
        }
        sendBroadcast(intent)
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            val context = this@NotificationService
            val player = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI)
            val db = LocalRepo.getInstance(context)
            // Setup notifications
            val channel = NotificationChannel("AFitNotification", "AFit", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val builder = Notification.Builder(context, channel.id)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            }
            // Service loop
            while (intentSound != null) {
                delay(200.milliseconds)

                var obj: SavedWorkout? = db.savedWorkout
                when (obj?.stage) {
                    TrainingStage.DoExercise.name -> {
                        showNotification(builder, obj.msg)
                    }
                    TrainingStage.Rest.name -> {
                        var timer = obj.restTime
                        // Ticking loop
                        while (timer > 0 && obj?.stage == TrainingStage.Rest.name) {
                            sendBroadcastCustom(timer, false)
                            showNotification(builder, "${obj.msg}: $timer")
                            timer -= 1
                            if (timer == 1) player.start()
                            obj = db.savedWorkout
                            delay(1.seconds)
                        }
                        if (obj?.stage == TrainingStage.Rest.name) {
                            db.savedWorkout = obj.copy(stage = TrainingStage.DoExercise.name)
                            sendBroadcastCustom(timer, true)
                        }
                    }
                }
            }
            launch {
                delay(1.seconds)
                player.release()
            }
            // Stop notification after
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun showNotification(
        builder: Notification.Builder,
        msg: String
    ) {
        val notification = builder
            .setContentTitle(msg)
            .setSmallIcon(R.drawable.ic_rv_exercise)
            .build()
        startForeground(1, notification)
    }
}