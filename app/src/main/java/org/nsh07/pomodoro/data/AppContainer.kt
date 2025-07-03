package org.nsh07.pomodoro.data

import android.content.Context

interface AppContainer {
    val appPreferencesRepository: AppPreferenceRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val appPreferencesRepository: AppPreferenceRepository by lazy {
        AppPreferenceRepository(
            AppDatabase.getDatabase(context).preferenceDao()
        )
    }

}