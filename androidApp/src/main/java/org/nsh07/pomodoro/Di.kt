package org.nsh07.pomodoro

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.nsh07.pomodoro.billing.BillingManager
import org.nsh07.pomodoro.billing.BillingManagerProvider
import org.nsh07.pomodoro.data.AppDatabase
import org.nsh07.pomodoro.data.AppPreferenceRepository
import org.nsh07.pomodoro.data.AppStatRepository
import org.nsh07.pomodoro.data.PreferenceRepository
import org.nsh07.pomodoro.data.StatRepository
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.service.ServiceHelper
import org.nsh07.pomodoro.service.addTimerActions
import org.nsh07.pomodoro.ui.settingsScreen.screens.backupRestore.viewModel.BackupRestoreViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerViewModel


class TimerStateHolder(private val stateRepository: StateRepository) {
    val time: MutableStateFlow<Long> by lazy {
        MutableStateFlow(stateRepository.settingsState.value.focusTime)
    }
}

class ActivityCallbacks {
    var activityTurnScreenOn: (Boolean) -> Unit = {}
}

val dbModule = module {
    single<AppDatabase> { create(::createDatabase) }
    single { get<AppDatabase>().preferenceDao() }
    single { get<AppDatabase>().statDao() }
    single { get<AppDatabase>().systemDao() }
}

val servicesModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }

    single<AppStatRepository>() bind StatRepository::class
    single<AppPreferenceRepository>() bind PreferenceRepository::class
    single<StateRepository>()
    single<BillingManager> { BillingManagerProvider.manager }
    single<ServiceHelper>()

    single { NotificationManagerCompat.from(get()) }
    single { create(::createNotificationManager) }
    single { create(::createNotificationCompatBuilder) }

    single<TimerStateHolder>()
    single<ActivityCallbacks>()
}

val viewModels = module {
    viewModel<BackupRestoreViewModel>()
    viewModel<TimerViewModel>()
    viewModel<SettingsViewModel>()
    viewModel<StatsViewModel>()
}

private fun createDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).build()
}

private fun createNotificationManager(context: Context): NotificationManager {
    return context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
}

private fun createNotificationCompatBuilder(context: Context): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, "timer")
        .setSmallIcon(R.drawable.tomato_logo_notification)
        .setColor(Color.Red.toArgb())
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addTimerActions(context, R.drawable.play, context.getString(R.string.start))
        .setShowWhen(true)
        .setSilent(true)
        .setOngoing(true)
        .setRequestPromotedOngoing(true)
        .setVisibility(VISIBILITY_PUBLIC)
}
