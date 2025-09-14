package org.nsh07.pomodoro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import org.nsh07.pomodoro.data.AppContainer
import org.nsh07.pomodoro.data.DefaultAppContainer

class TomatoApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        val notificationChannel = NotificationChannel(
            "timer",
            "Timer progress",
            NotificationManager.IMPORTANCE_HIGH
        )

        container.notificationManager.createNotificationChannel(notificationChannel)
    }
}