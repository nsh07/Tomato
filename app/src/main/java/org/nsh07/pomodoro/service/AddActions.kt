/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import org.nsh07.pomodoro.R

fun NotificationCompat.Builder.addTimerActions(
    context: Context,
    @DrawableRes playPauseIcon: Int,
    playPauseText: String
): NotificationCompat.Builder = this
    .addAction(
        playPauseIcon,
        playPauseText,
        PendingIntent.getService(
            context,
            0,
            Intent(context, TimerService::class.java).also {
                it.action = TimerService.Actions.TOGGLE.toString()
            },
            FLAG_IMMUTABLE
        )
    )
    .addAction(
        R.drawable.restart,
        context.getString(R.string.exit),
        PendingIntent.getService(
            context,
            0,
            Intent(context, TimerService::class.java).also {
                it.action = TimerService.Actions.RESET.toString()
            },
            FLAG_IMMUTABLE
        )
    )
    .addAction(
        R.drawable.skip_next,
        context.getString(R.string.skip),
        PendingIntent.getService(
            context,
            0,
            Intent(context, TimerService::class.java).also {
                it.action = TimerService.Actions.SKIP.toString()
            },
            FLAG_IMMUTABLE
        )
    )

fun NotificationCompat.Builder.addStopAlarmAction(
    context: Context
): NotificationCompat.Builder = this
    .addAction(
        R.drawable.alarm,
        context.getString(R.string.stop_alarm),
        PendingIntent.getService(
            context,
            0,
            Intent(context, TimerService::class.java).also {
                it.action = TimerService.Actions.STOP_ALARM.toString()
            },
            FLAG_IMMUTABLE
        )
    )