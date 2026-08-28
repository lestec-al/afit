package com.yurhel.alex.afit.ui.screen_training

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yurhel.alex.afit.R
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.NotificationService
import com.yurhel.alex.afit.data.Obj
import com.yurhel.alex.afit.data.SavedWorkout
import java.util.Date

class TrainingViewModel(
    private val localRepo: LocalRepo,
    objId: Int,
    activity: Activity
): ViewModel() {
    class Factory(private val localRepo: LocalRepo, private val objId: Int, private val activity: Activity): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TrainingViewModel(localRepo, objId, activity) as T
    }

    val mainObj: Obj = localRepo.getOneMainObj(objId, true)

    var stage by mutableStateOf(TrainingStage.DoExercise)
        private set

    var repsList by mutableStateOf(listOf<String>())
        private set

    var weightsList by mutableStateOf(listOf<String>())
        private set

    var repsText by mutableStateOf("${mainObj.reps}")
        private set

    var withWeight by mutableStateOf(localRepo.getIsWeightShowForMainStat(objId))
        private set

    var weight by mutableStateOf("${mainObj.weight.toInt()}")
        private set

    var timeAtStartOfRest = mainObj.rest

    var time by mutableIntStateOf(mainObj.rest)
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            time = intent?.getIntExtra("COUNTER", time) ?: time
            val restEnd = intent?.getBooleanExtra("REST_END", false) == true
            if (restEnd) {
                stage = TrainingStage.DoExercise
                saveWorkoutTemp("${context?.getString(R.string.do_exercise)} $repsText ${mainObj.name}")
            }
        }
    }

    var exerciseEndAndIsSaveResult by mutableStateOf<Boolean?>(null)
        private set

    var isEditSheetOpens by mutableStateOf(false)
        private set
    fun updateIsEditSheetOpens(value: Boolean) {
        isEditSheetOpens = value
    }
    fun saveWeight(value: String) {
        if (value.isNotEmpty()) {
            try {
                weight = "${value.toInt()}"
            } catch (_: Exception) {}
        }
    }

    private var wakeLock: WakeLock? = null
    private var startTime: Long = System.currentTimeMillis()

    fun mainButtonClick(context: Context) {
        when(stage) {
            TrainingStage.DoExercise -> {
                stage = TrainingStage.Rest
                textToProgress(false)
                if (withWeight) textToProgress(true)
                if (repsList.size >= mainObj.sets) {
                    exit(true, context)
                } else {
                    saveWorkoutTemp(context.getString(R.string.rest))
                }
            }
            TrainingStage.Rest -> {
                stage = TrainingStage.DoExercise
                saveWorkoutTemp("${context.getString(R.string.do_exercise)} $repsText ${mainObj.name}")
            }
        }
    }

    fun minusButtonClick() {
        val i = repsText.toInt()
        if (i > 0) repsText = "${repsText.toInt() - 1}"
    }

    fun plusButtonClick() {
        val i = repsText.toInt()
        if (i >= 0) repsText = "${repsText.toInt() + 1}"
    }

    fun exit(
        isSaveResults: Boolean,
        context: Context
    ) {
        // Stop loop & notification ???
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        exerciseEndAndIsSaveResult = isSaveResults && repsList.isNotEmpty()
        if (wakeLock?.isHeld == true) wakeLock!!.release()
        NotificationService.stop(context)
        localRepo.clearSavedWorkout()

        // Save results
        if (isSaveResults && repsList.isNotEmpty()) {
            var resultShort = 0
            for (i in repsList) {
                resultShort += i.toInt()
            }
            val resultFull: String = java.lang.String.join(" + ", repsList)
            val resultWeights: String = java.lang.String.join(" + ", weightsList)
            val trainingTime: String
            val timeSec = (System.currentTimeMillis() - startTime) / 1000
            if (timeSec >= 60) {
                val min = timeSec / 60
                val sec = timeSec % 60
                trainingTime = min.toString() + ":" + (if (sec < 10) "0$sec" else sec.toString())
            } else {
                trainingTime = "0:" + (if (timeSec < 10) "0$timeSec" else timeSec.toString())
            }
            val date = Date().time.toString()
            localRepo.addExerciseEntry(
                mainObj.id,
                resultShort,
                resultFull,
                trainingTime,
                date,
                resultWeights
            )
        }
    }

    private fun textToProgress(isWeight: Boolean) {
        if (isWeight) {
            val listEdit = weightsList.toMutableList()
            listEdit.add(weight)
            weightsList = listEdit.toList()
        } else {
            val listEdit = repsList.toMutableList()
            listEdit.add(repsText)
            repsList = listEdit.toList()
        }
    }

    private fun saveWorkoutTemp(msg: String) {
        var repsListStr = ""
        repsList.forEach {
            repsListStr += "$it "
        }
        var weightsListStr = ""
        weightsList.forEach {
            weightsListStr += "$it "
        }
        localRepo.savedWorkout = SavedWorkout(
            stage = stage.name,
            msg = msg,
            restTime = timeAtStartOfRest,
            exId = mainObj.id,
            repsList = repsListStr,
            weightsList = weightsListStr,
            startTime = startTime
        )
    }

    init {
        // Connect to service
        val filter = IntentFilter("NOTIFICATION")
        ContextCompat.registerReceiver(activity, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        // Check permission
        activity.requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        // Check saved workout
        val savedWorkout: SavedWorkout? = localRepo.savedWorkout
        if (savedWorkout != null) {
            // Try to restore workout
            stage = when (savedWorkout.stage) {
                TrainingStage.Rest.name -> TrainingStage.Rest
                else -> TrainingStage.DoExercise
            }
            repsList = savedWorkout.repsList.split(" ").filter {
                it != "" && it != " "
            }
            weightsList = savedWorkout.weightsList.split(" ").filter {
                it != "" && it != " "
            }
            startTime = savedWorkout.startTime
        } else {
            // Save new workout
            saveWorkoutTemp("${activity.getString(R.string.do_exercise)} $repsText ${mainObj.name}")
        }
        // Start service
        NotificationService.start(activity)
        // Setup wake lock
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AFit::WakelockKeepTag"
        )
        wakeLock!!.acquire(3600000L /*1 hour*/)
    }
}