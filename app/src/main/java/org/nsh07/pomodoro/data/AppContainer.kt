package org.nsh07.pomodoro.data

import android.content.Context

interface AppContainer {
    val appPreferencesRepository: AppPreferenceRepository
    val appTimerRepository: AppTimerRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val appPreferencesRepository: AppPreferenceRepository by lazy {
        AppPreferenceRepository(
            AppDatabase.getDatabase(context).preferenceDao()
        )
    }

    override val appTimerRepository: AppTimerRepository by lazy {
        AppTimerRepository()
    }

}