package com.yurhel.alex.afit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yurhel.alex.afit.data.LocalRepo
import com.yurhel.alex.afit.data.SavedWorkout
import com.yurhel.alex.afit.data.StorageRepo
import com.yurhel.alex.afit.data.RemoteRepo
import com.yurhel.alex.afit.ui.help.Screen
import com.yurhel.alex.afit.ui.help.nav
import com.yurhel.alex.afit.ui.screen_main.MainScreen
import com.yurhel.alex.afit.ui.screen_main.MainViewModel
import com.yurhel.alex.afit.ui.screen_main.calendar.CalendarCardViewModel
import com.yurhel.alex.afit.ui.screen_settings.SettingScreen
import com.yurhel.alex.afit.ui.screen_settings.SettingsViewModel
import com.yurhel.alex.afit.ui.screen_stats.StatsScreen
import com.yurhel.alex.afit.ui.screen_stats.StatsViewModel
import com.yurhel.alex.afit.ui.screen_training.TrainingScreen
import com.yurhel.alex.afit.ui.screen_training.TrainingViewModel
import com.yurhel.alex.afit.ui.theme.AFitTheme

class MainActivity : AppCompatActivity() {

    override fun onDestroy() {
        LocalRepo.getInstance(this).closeDB()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val context = this
        val localRepo = LocalRepo.getInstance(context)
        val savedWorkout: SavedWorkout? = localRepo.savedWorkout

        setContent {
            AFitTheme {
                val nav = rememberNavController()
                NavHost(
                    navController = nav,
                    startDestination = if (savedWorkout == null) Screen.Main.name else {
                        "${Screen.Training.name}/${savedWorkout.exId}"
                    },
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Main screen
                    composable(Screen.Main.name) {
                        MainScreen(
                            onBack = ::finishAffinity,
                            onSettings = { nav.nav(Screen.Settings.name) },
                            onCard = { type, id ->
                                nav.nav("${Screen.Stats.name}/${type}/${id}/${false}")
                            },
                            calendarVm = viewModel(factory = CalendarCardViewModel.Factory(localRepo)),
                            vm = viewModel(factory = MainViewModel.Factory(localRepo))
                        )
                    }

                    // Settings screen
                    composable(Screen.Settings.name) {
                        SettingScreen(
                            onBack = { nav.nav(Screen.Main.name) },
                            vm = viewModel(factory = SettingsViewModel.Factory(
                                localRepo, RemoteRepo(context), StorageRepo(context)
                            ))
                        )
                    }

                    // Stats screen
                    composable(
                        route = "${Screen.Stats.name}/{type}/{id}/{afterWorkout}",
                        arguments = listOf(
                            navArgument("type") { type = NavType.StringType },
                            navArgument("id") { type = NavType.IntType },
                            navArgument("afterWorkout") { type = NavType.BoolType },
                        )
                    ) {
                        val type = it.arguments?.getString("type") ?: ""
                        val id = it.arguments?.getInt("id") ?: 0
                        val afterWorkout = it.arguments?.getBoolean("afterWorkout") == true

                        StatsScreen(
                            onBack = { nav.nav(Screen.Main.name) },
                            onWorkout = { nav.nav("${Screen.Training.name}/${id}") },
                            vm = viewModel(factory = StatsViewModel.Factory(
                                localRepo, type, id, afterWorkout
                            ))
                        )
                    }

                    // Training screen
                    composable(
                        route = "${Screen.Training.name}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) {
                        val id = it.arguments?.getInt("id") ?: 0

                        TrainingScreen(
                            onBack = { isPastExerciseSaved ->
                                nav.nav("${Screen.Stats.name}/ex_id/${id}/${isPastExerciseSaved}")
                            },
                            vm = viewModel(factory = TrainingViewModel.Factory(
                                localRepo, id, context
                            ))
                        )
                    }
                }
            }
        }
    }
}