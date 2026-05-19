package com.yurhel.alex.afit.ui.screen_training

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
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.SavedWorkout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationService : Service() {
    companion object {
        var isWorkoutRunning: Boolean = false
        private var intent: Intent? = null

        fun start(context: Context) {
            if (!isWorkoutRunning) {
                isWorkoutRunning = true
                intent = Intent(context, NotificationService::class.java)
                context.startService(intent)
            }
        }
        fun stop(context: Context) {
            context.stopService(intent)
            intent = null
            isWorkoutRunning = false
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
            while (isWorkoutRunning) {
                delay(200)

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
                            delay(1000)
                        }
                        if (obj?.stage == TrainingStage.Rest.name) {
                            db.savedWorkout = obj.copy(stage = TrainingStage.DoExercise.name)
                            sendBroadcastCustom(timer, true)
                        }
                    }
                }
            }
            launch {
                delay(1000)
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