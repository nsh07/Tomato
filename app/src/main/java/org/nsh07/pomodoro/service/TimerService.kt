/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.utils.millisecondsToStr
import kotlin.text.Typography.middleDot

class TimerService : Service() {
    private val appContainer by lazy {
        (application as TomatoApplication).container
    }

    private val timerRepository by lazy { appContainer.appTimerRepository }
    private val statRepository by lazy { appContainer.appStatRepository }
    private val notificationManager by lazy { appContainer.notificationManager }
    private val notificationManagerService by lazy { appContainer.notificationManagerService }
    private val notificationBuilder by lazy { appContainer.notificationBuilder }
    private val _timerState by lazy { appContainer.timerState }
    private val _time by lazy { appContainer.time }

    private val timeStateFlow by lazy { _time.asStateFlow() }

    private var time: Long
        get() = timeStateFlow.value
        set(value) = _time.update { value }

    private val timerState by lazy { _timerState.asStateFlow() }

    private var cycles = 0
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L

    private var job = SupervisorJob()
    private val timerScope = CoroutineScope(Dispatchers.IO + job)
    private val skipScope = CoroutineScope(Dispatchers.IO + job)

    private var autoAlarmStopScope: Job? = null

    private var alarm: MediaPlayer? = null
    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val cs by lazy { timerRepository.colorScheme }

    private lateinit var notificationStyle: NotificationCompat.ProgressStyle

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        timerRepository.serviceRunning.update { true }
        alarm = initializeMediaPlayer()
    }

    override fun onDestroy() {
        timerRepository.serviceRunning.update { false }
        runBlocking {
            job.cancel()
            saveTimeToDb()
            setDoNotDisturb(false)
            notificationManager.cancel(1)
            alarm?.release()
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.TOGGLE.toString() -> {
                startForegroundService()
                toggleTimer()
            }

            Actions.RESET.toString() -> {
                if (timerState.value.timerRunning) toggleTimer()
                skipScope.launch {
                    resetTimer()
                    stopForegroundService()
                }
            }

            Actions.SKIP.toString() -> skipScope.launch { skipTimer(true) }

            Actions.STOP_ALARM.toString() -> stopAlarm()

            Actions.UPDATE_ALARM_TONE.toString() -> updateAlarmTone()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun toggleTimer() {
        updateProgressSegments()

        if (timerState.value.timerRunning) {
            setDoNotDisturb(false)
            notificationBuilder.clearActions().addTimerActions(
                this, R.drawable.play, getString(R.string.start)
            )
            showTimerNotification(time.toInt(), paused = true)
            _timerState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            pauseTime = SystemClock.elapsedRealtime()
        } else {
            if (timerState.value.timerMode == TimerMode.FOCUS) setDoNotDisturb(true)
            else setDoNotDisturb(false)
            notificationBuilder.clearActions().addTimerActions(
                this, R.drawable.pause, getString(R.string.stop)
            )
            _timerState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) pauseDuration += SystemClock.elapsedRealtime() - pauseTime

            var iterations = -1

            timerScope.launch {
                while (true) {
                    if (!timerState.value.timerRunning) break
                    if (startTime == 0L) startTime = SystemClock.elapsedRealtime()

                    time = when (timerState.value.timerMode) {
                        TimerMode.FOCUS -> timerRepository.focusTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)

                        TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)

                        else -> timerRepository.longBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)
                    }

                    iterations =
                        (iterations + 1) % timerRepository.timerFrequency.toInt().coerceAtLeast(1)

                    if (iterations == 0) showTimerNotification(time.toInt())

                    if (time < 0) {
                        skipTimer()
                        _timerState.update { currentState ->
                            currentState.copy(timerRunning = false)
                        }
                        break
                    } else {
                        _timerState.update { currentState ->
                            currentState.copy(
                                timeStr = millisecondsToStr(time)
                            )
                        }
                    }

                    delay((1000f / timerRepository.timerFrequency).toLong())
                }
            }
        }
    }

    @SuppressLint(
        "MissingPermission",
        "StringFormatInvalid"
    ) // We check for the permission when pressing the Play button in the UI
    fun showTimerNotification(
        remainingTime: Int, paused: Boolean = false, complete: Boolean = false
    ) {
        if (complete) notificationBuilder.clearActions().addStopAlarmAction(this)

        val totalTime = when (timerState.value.timerMode) {
            TimerMode.FOCUS -> timerRepository.focusTime.toInt()
            TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime.toInt()
            else -> timerRepository.longBreakTime.toInt()
        }

        val currentTimer = when (timerState.value.timerMode) {
            TimerMode.FOCUS -> getString(R.string.focus)
            TimerMode.SHORT_BREAK -> getString(R.string.short_break)
            else -> getString(R.string.long_break)
        }

        val nextTimer = when (timerState.value.nextTimerMode) {
            TimerMode.FOCUS -> getString(R.string.focus)
            TimerMode.SHORT_BREAK -> getString(R.string.short_break)
            else -> getString(R.string.long_break)
        }

        val remainingTimeString = if ((remainingTime.toFloat() / 60000f) < 1.0f) "< 1"
        else (remainingTime.toFloat() / 60000f).toInt()

        notificationManager.notify(
            1,
            notificationBuilder
                .setContentTitle(
                    if (!complete) {
                        "$currentTimer  $middleDot  ${
                            getString(R.string.min_remaining_notification, remainingTimeString)
                        }" + if (paused) "  $middleDot  ${getString(R.string.paused)}" else ""
                    } else "$currentTimer $middleDot ${getString(R.string.completed)}"
                )
                .setContentText(
                    getString(
                        R.string.up_next_notification,
                        nextTimer,
                        timerState.value.nextTimeStr
                    )
                )
                .setStyle(
                    notificationStyle
                        .setProgress( // Set the current progress by filling the previous intervals and part of the current interval
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                                (totalTime - remainingTime) + ((cycles + 1) / 2) * timerRepository.focusTime.toInt() + (cycles / 2) * timerRepository.shortBreakTime.toInt()
                            } else (totalTime - remainingTime)
                        )
                )
                .setWhen(System.currentTimeMillis() + remainingTime) // Sets the Live Activity/Now Bar chip time
                .setShortCriticalText(millisecondsToStr(time.coerceAtLeast(0)))
                .build()
        )

        if (complete) {
            startAlarm()
            _timerState.update { currentState ->
                currentState.copy(alarmRinging = true)
            }
        }
    }

    private fun updateProgressSegments() {
        notificationStyle = NotificationCompat.ProgressStyle()
            .also {
                // Add all the Focus, Short break and long break intervals in order
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    // Android 16 and later supports live updates
                    // Set progress bar sections if on Baklava or later
                    for (i in 0..<timerRepository.sessionLength * 2) {
                        if (i % 2 == 0) it.addProgressSegment(
                            NotificationCompat.ProgressStyle.Segment(
                                timerRepository.focusTime.toInt()
                            )
                                .setColor(cs.primary.toArgb())
                        )
                        else if (i != (timerRepository.sessionLength * 2 - 1)) it.addProgressSegment(
                            NotificationCompat.ProgressStyle.Segment(
                                timerRepository.shortBreakTime.toInt()
                            ).setColor(cs.tertiary.toArgb())
                        )
                        else it.addProgressSegment(
                            NotificationCompat.ProgressStyle.Segment(
                                timerRepository.longBreakTime.toInt()
                            ).setColor(cs.tertiary.toArgb())
                        )
                    }
                } else {
                    it.addProgressSegment(
                        NotificationCompat.ProgressStyle.Segment(
                            when (timerState.value.timerMode) {
                                TimerMode.FOCUS -> timerRepository.focusTime.toInt()
                                TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime.toInt()
                                else -> timerRepository.longBreakTime.toInt()
                            }
                        )
                    )
                }
            }
    }

    private suspend fun resetTimer() {
        updateProgressSegments()
        saveTimeToDb()
        time = timerRepository.focusTime
        cycles = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        _timerState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = millisecondsToStr(time),
                totalTime = time,
                nextTimerMode = if (timerRepository.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                nextTimeStr = millisecondsToStr(if (timerRepository.sessionLength > 1) timerRepository.shortBreakTime else timerRepository.longBreakTime),
                currentFocusCount = 1,
                totalFocusCount = timerRepository.sessionLength
            )
        }
    }

    private suspend fun skipTimer(fromButton: Boolean = false) {
        updateProgressSegments()
        saveTimeToDb()
        updateProgressSegments()
        showTimerNotification(0, paused = true, complete = !fromButton)
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        cycles = (cycles + 1) % (timerRepository.sessionLength * 2)

        if (cycles % 2 == 0) {
            if (timerState.value.timerRunning) setDoNotDisturb(true)
            time = timerRepository.focusTime
            _timerState.update { currentState ->
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = millisecondsToStr(time),
                    totalTime = time,
                    nextTimerMode = if (cycles == (timerRepository.sessionLength - 1) * 2) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    nextTimeStr = if (cycles == (timerRepository.sessionLength - 1) * 2) millisecondsToStr(
                        timerRepository.longBreakTime
                    ) else millisecondsToStr(
                        timerRepository.shortBreakTime
                    ),
                    currentFocusCount = cycles / 2 + 1,
                    totalFocusCount = timerRepository.sessionLength
                )
            }
        } else {
            if (timerState.value.timerRunning) setDoNotDisturb(false)
            val long = cycles == (timerRepository.sessionLength * 2) - 1
            time = if (long) timerRepository.longBreakTime else timerRepository.shortBreakTime

            _timerState.update { currentState ->
                currentState.copy(
                    timerMode = if (long) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    timeStr = millisecondsToStr(time),
                    totalTime = time,
                    nextTimerMode = TimerMode.FOCUS,
                    nextTimeStr = millisecondsToStr(timerRepository.focusTime)
                )
            }
        }
    }

    fun startAlarm() {
        if (timerRepository.alarmEnabled) alarm?.start()

        appContainer.activityTurnScreenOn(true)

        autoAlarmStopScope = CoroutineScope(Dispatchers.IO).launch {
            delay(1 * 60 * 1000)
            stopAlarm()
        }

        if (timerRepository.vibrateEnabled) {
            if (!vibrator.hasVibrator()) {
                return
            }
            val vibrationPattern = longArrayOf(0, 1000, 1000, 1000)
            val repeat = 2
            val effect = VibrationEffect.createWaveform(vibrationPattern, repeat)
            vibrator.vibrate(effect)
        }
    }

    fun stopAlarm() {
        autoAlarmStopScope?.cancel()

        if (timerRepository.alarmEnabled) {
            alarm?.pause()
            alarm?.seekTo(0)
        }

        if (timerRepository.vibrateEnabled) {
            vibrator.cancel()
        }

        appContainer.activityTurnScreenOn(false)

        _timerState.update { currentState ->
            currentState.copy(alarmRinging = false)
        }
        notificationBuilder.clearActions().addTimerActions(
            this, R.drawable.play,
            getString(R.string.start_next)
        )
        showTimerNotification(
            when (timerState.value.timerMode) {
                TimerMode.FOCUS -> timerRepository.focusTime.toInt()
                TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime.toInt()
                else -> timerRepository.longBreakTime.toInt()
            }, paused = true, complete = false
        )
    }

    private fun initializeMediaPlayer(): MediaPlayer? {
        return try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                timerRepository.alarmSoundUri?.let {
                    setDataSource(applicationContext, it)
                    prepare()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setDoNotDisturb(doNotDisturb: Boolean) {
        if (timerRepository.dndEnabled && notificationManagerService.isNotificationPolicyAccessGranted()) {
            if (doNotDisturb) {
                notificationManagerService.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
            } else notificationManagerService.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    private fun updateAlarmTone() {
        alarm?.release()
        alarm = initializeMediaPlayer()
    }

    suspend fun saveTimeToDb() {
        when (timerState.value.timerMode) {
            TimerMode.FOCUS -> statRepository.addFocusTime(
                (timerState.value.totalTime - time).coerceAtLeast(
                    0L
                )
            )

            else -> statRepository.addBreakTime((timerState.value.totalTime - time).coerceAtLeast(0L))
        }
    }

    private fun startForegroundService() {
        startForeground(1, notificationBuilder.build())
    }

    private fun stopForegroundService() {
        notificationManager.cancel(1)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    enum class Actions {
        TOGGLE, SKIP, RESET, STOP_ALARM, UPDATE_ALARM_TONE
    }
}
