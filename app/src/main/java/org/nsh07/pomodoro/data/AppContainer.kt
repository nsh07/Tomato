/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import android.app.PendingIntent
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.service.addTimerActions
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import org.nsh07.pomodoro.utils.millisecondsToStr

interface AppContainer {
    val appPreferenceRepository: AppPreferenceRepository
    val appStatRepository: AppStatRepository
    val appTimerRepository: AppTimerRepository
    val notificationManager: NotificationManagerCompat
    val notificationBuilder: NotificationCompat.Builder
    val timerState: MutableStateFlow<TimerState>
    val time: MutableStateFlow<Long>
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val appPreferenceRepository: AppPreferenceRepository by lazy {
        AppPreferenceRepository(AppDatabase.getDatabase(context).preferenceDao())
    }

    override val appStatRepository: AppStatRepository by lazy {
        AppStatRepository(AppDatabase.getDatabase(context).statDao())
    }

    override val appTimerRepository: AppTimerRepository by lazy { AppTimerRepository() }

    override val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    override val notificationBuilder: NotificationCompat.Builder by lazy {
        NotificationCompat.Builder(context, "timer")
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
            .addTimerActions(context, R.drawable.play, "Start")
            .setShowWhen(true)
            .setSilent(true)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
    }

    override val timerState: MutableStateFlow<TimerState> by lazy {
        MutableStateFlow(
            TimerState(
                totalTime = appTimerRepository.focusTime,
                timeStr = millisecondsToStr(appTimerRepository.focusTime),
                nextTimeStr = millisecondsToStr(appTimerRepository.shortBreakTime)
            )
        )
    }

    override val time: MutableStateFlow<Long> by lazy {
        MutableStateFlow(appTimerRepository.focusTime)
    }

}