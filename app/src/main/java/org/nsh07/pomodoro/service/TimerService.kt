package org.nsh07.pomodoro.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
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
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val skipScope = CoroutineScope(Dispatchers.IO + job)

    private val alarm by lazy {
        MediaPlayer.create(
            this, Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_RINGTONE_URI
        )
    }

    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private var cs: ColorScheme = lightColorScheme()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.TOGGLE.toString() -> {
                startForegroundService()
                toggleTimer()
            }

            Actions.RESET.toString() -> {
                if (timerState.value.timerRunning) toggleTimer()
                resetTimer()
                stopForegroundService()
            }

            Actions.SKIP.toString() -> skipTimer(true)

            Actions.STOP_ALARM.toString() -> stopAlarm()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun toggleTimer() {
        if (timerState.value.timerRunning) {
            notificationBuilder.clearActions().addTimerActions(
                this, R.drawable.play, "Start"
            )
            showTimerNotification(time.toInt(), paused = true)
            _timerState.update { currentState ->
                currentState.copy(timerRunning = false)
            }
            pauseTime = SystemClock.elapsedRealtime()
        } else {
            notificationBuilder.clearActions().addTimerActions(
                this, R.drawable.pause, "Stop"
            )
            _timerState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) pauseDuration += SystemClock.elapsedRealtime() - pauseTime

            var iterations = -1

            scope.launch {
                while (true) {
                    if (!timerState.value.timerRunning) break
                    if (startTime == 0L) startTime = SystemClock.elapsedRealtime()

                    time = when (timerState.value.timerMode) {
                        TimerMode.FOCUS -> timerRepository.focusTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()

                        TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()

                        else -> timerRepository.longBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration).toInt()
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

    @SuppressLint("MissingPermission") // We check for the permission when pressing the Play button in the UI
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
            TimerMode.FOCUS -> "Focus"
            TimerMode.SHORT_BREAK -> "Short break"
            else -> "Long break"
        }

        val nextTimer = when (timerState.value.nextTimerMode) {
            TimerMode.FOCUS -> "Focus"
            TimerMode.SHORT_BREAK -> "Short break"
            else -> "Long break"
        }

        val remainingTimeString = if ((remainingTime.toFloat() / 60000f) < 1.0f) "< 1"
        else (remainingTime.toFloat() / 60000f).toInt()

        notificationManager.notify(
            1, notificationBuilder.setContentTitle(
                if (!complete) {
                    "$currentTimer $middleDot  $remainingTimeString min remaining" + if (paused) "  $middleDot  Paused" else ""
                } else "$currentTimer $middleDot Completed"
            ).setContentText("Up next: $nextTimer (${timerState.value.nextTimeStr})")
                .setStyle(NotificationCompat.ProgressStyle().also {
                    // Add all the Focus, Short break and long break intervals in order
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        // Android 16 and later supports live updates
                        // Set progress bar sections if on Baklava or later
                        for (i in 0..<timerRepository.sessionLength * 2) {
                            if (i % 2 == 0) it.addProgressSegment(
                                NotificationCompat.ProgressStyle.Segment(
                                    timerRepository.focusTime.toInt()
                                ).setColor(cs.primary.toArgb())
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
                    .setProgress( // Set the current progress by filling the previous intervals and part of the current interval
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                            (totalTime - remainingTime) + ((cycles + 1) / 2) * timerRepository.focusTime.toInt() + (cycles / 2) * timerRepository.shortBreakTime.toInt()
                        } else (totalTime - remainingTime)
                    ))
                .setWhen(System.currentTimeMillis() + remainingTime) // Sets the Live Activity/Now Bar chip time
                .setShortCriticalText(millisecondsToStr(time.coerceAtLeast(0))).build())

        if (complete) {
            startAlarm()
            _timerState.update { currentState ->
                currentState.copy(alarmRinging = true)
            }
        }
    }

    private fun resetTimer() {
        skipScope.launch {
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
    }

    private fun skipTimer(fromButton: Boolean = false) {
        skipScope.launch {
            saveTimeToDb()
            showTimerNotification(0, paused = true, complete = !fromButton)
            startTime = 0L
            pauseTime = 0L
            pauseDuration = 0L

            cycles = (cycles + 1) % (timerRepository.sessionLength * 2)

            if (cycles % 2 == 0) {
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
    }

    fun startAlarm() {
        if (timerRepository.alarmEnabled) alarm.start()

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
        if (timerRepository.alarmEnabled) {
            alarm.pause()
            alarm.seekTo(0)
        }

        if (timerRepository.vibrateEnabled) {
            vibrator.cancel()
        }

        _timerState.update { currentState ->
            currentState.copy(alarmRinging = false)
        }
        notificationBuilder.clearActions().addTimerActions(this, R.drawable.play, "Start next")
        showTimerNotification(
            when (timerState.value.timerMode) {
                TimerMode.FOCUS -> timerRepository.focusTime.toInt()
                TimerMode.SHORT_BREAK -> timerRepository.shortBreakTime.toInt()
                else -> timerRepository.longBreakTime.toInt()
            }, paused = true, complete = false
        )
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

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            job.cancel()
            saveTimeToDb()
            notificationManager.cancel(1)
        }
    }

    enum class Actions {
        TOGGLE, SKIP, RESET, STOP_ALARM
    }
}