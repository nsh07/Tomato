/*
 * Copyright (c) 2026 Nishant Mishra
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

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.nsh07.pomodoro.data.StateRepository
import org.nsh07.pomodoro.data.Topic
import org.nsh07.pomodoro.data.TopicShape
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import kotlin.coroutines.CoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class TimerManagerTest {

    /** Swallows everything submitted to it, so that the timer loop never executes */
    private object NeverDispatcher : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) = Unit
    }

    private val topic = Topic(
        id = 42L,
        name = "Test",
        color = Color.White,
        shape = TopicShape.CIRCLE,
        focusTime = MINUTE * 25,
        shortBreakTime = MINUTE * 5,
        longBreakTime = MINUTE * 15,
        sessionLength = 4,
        autostartNextSession = false,
        dndEnabled = false
    )

    /** A device that has been up for a week */
    private var clock = 7L * 24 * 60 * MINUTE

    private lateinit var statRepository: FakeStatRepository
    private lateinit var stateRepository: StateRepository
    private lateinit var timerManager: TimerManager

    private val loopScope = CoroutineScope(NeverDispatcher)

    /** The wakeup the platform has been asked to schedule, or null if cancelled */
    private var scheduledExpiry: Long? = null

    @BeforeTest
    fun setUp() = runBlocking {
        statRepository = FakeStatRepository()
        stateRepository = StateRepository(
            FakePreferenceRepository(currentTopicId = topic.id),
            FakeTopicRepository(topic)
        )
        // Wait for the settings load started by the constructor, which ends by publishing the
        // topic's focus time as the total time
        withTimeout(TIMEOUT.milliseconds) {
            while (stateRepository.timerState.value.totalTime != topic.focusTime) yield()
        }
        scheduledExpiry = null
        timerManager = TimerManager(stateRepository, statRepository, { clock }) {
            scheduledExpiry = it
        }
    }

    @Test
    fun `focus time recorded matches the time actually spent focusing`() = runBlocking {
        timerManager.toggle()

        clock += 5 * MINUTE
        timerManager.saveTimeToDb()
        assertEquals(5 * MINUTE, statRepository.focusTime)

        clock += 7 * MINUTE
        timerManager.saveTimeToDb()
        assertEquals(12 * MINUTE, statRepository.focusTime)
        assertEquals(0L, statRepository.breakTime)
    }

    @Test
    fun `paused time is not counted`() = runBlocking {
        timerManager.toggle()
        clock += 10 * MINUTE

        timerManager.toggle() // pause
        clock += 3 * 60 * MINUTE // the user goes away for three hours

        timerManager.toggle() // resume
        clock += 5 * MINUTE

        timerManager.saveTimeToDb()
        assertEquals(15 * MINUTE, statRepository.focusTime)
    }

    @Test
    fun `no time accrues while the timer is stopped`() = runBlocking {
        timerManager.toggle()
        clock += MINUTE
        timerManager.toggle() // pause
        timerManager.skip()

        clock += 12 * 60 * MINUTE // half a day with the timer sitting on a fresh interval

        timerManager.saveTimeToDb()
        assertEquals(MINUTE, statRepository.focusTime)
        assertEquals(0L, statRepository.breakTime)
    }

    /** Regression test for issue #278 */
    @Test
    fun `skipping does not record the device uptime`() = runBlocking {
        timerManager.toggle()
        clock += 2 * MINUTE

        timerManager.skip() // focus -> short break
        timerManager.saveTimeToDb()

        assertEquals(2 * MINUTE, statRepository.focusTime)
        assertEquals(0L, statRepository.breakTime)
    }

    /** Regression test for issue #299 */
    @Test
    fun `infinite focus never records the infinite sentinel`() = runBlocking {
        stateRepository.timerState.update {
            it.copy(
                infiniteFocus = true,
                totalTime = Long.MAX_VALUE
            )
        }

        timerManager.toggle()
        clock += 30 * MINUTE
        timerManager.skip() // infinite focus -> short break

        clock += 4 * MINUTE
        timerManager.skip() // short break -> infinite focus

        clock += 10 * MINUTE
        timerManager.saveTimeToDb()

        assertEquals(40 * MINUTE, statRepository.focusTime)
        assertEquals(4 * MINUTE, statRepository.breakTime)
    }

    /** Regression test for issue #299, with the interval switch interrupted halfway */
    @Test
    fun `a stale infinite total time cannot inflate a break`() = runBlocking {
        stateRepository.timerState.update { it.copy(infiniteFocus = true) }
        timerManager.toggle()

        // The mode is already the break, but the total time is still the infinite focus one
        stateRepository.timerState.update {
            it.copy(timerMode = TimerMode.SHORT_BREAK, totalTime = Long.MAX_VALUE)
        }
        clock += 3 * MINUTE
        timerManager.saveTimeToDb()

        assertEquals(3 * MINUTE, statRepository.breakTime)
        assertEquals(0L, statRepository.focusTime)
    }

    @Test
    fun `undoing a reset does not record the same time twice`() = runBlocking {
        timerManager.toggle()
        clock += 10 * MINUTE
        timerManager.toggle() // pause

        timerManager.resetTimer {}
        assertEquals(10 * MINUTE, statRepository.focusTime)

        timerManager.undoReset()
        timerManager.saveTimeToDb()
        assertEquals(10 * MINUTE, statRepository.focusTime)
    }

    @Test
    fun `undoing a reset that never happened does nothing`() = runBlocking {
        timerManager.toggle()
        clock += 10 * MINUTE

        timerManager.undoReset()
        timerManager.saveTimeToDb()

        assertEquals(10 * MINUTE, statRepository.focusTime)
    }

    @Test
    fun `an interval records at most its own length`() = runBlocking {
        timerManager.toggle()
        clock += 3 * 60 * MINUTE // the timer loop dies and three hours pass

        timerManager.saveTimeToDb()
        assertEquals(topic.focusTime, statRepository.focusTime)
    }

    @Test
    fun `a single write can never exceed a day`() = runBlocking {
        stateRepository.timerState.update { it.copy(infiniteFocus = true) }
        timerManager.toggle()
        clock += 30L * 24 * 60 * MINUTE // a month of uptime passes in one endless interval

        timerManager.saveTimeToDb()
        assertTrue(
            statRepository.focusTime <= 24 * 60 * MINUTE,
            "recorded ${statRepository.focusTime} ms in one write"
        )
    }

    @Test
    fun `starting the timer schedules the expiry alarm at the end of the interval`() = runBlocking {
        timerManager.toggle()
        assertEquals(clock + topic.focusTime, scheduledExpiry)
    }

    @Test
    fun `pausing cancels the expiry alarm`() = runBlocking {
        timerManager.toggle()
        clock += 5 * MINUTE

        timerManager.toggle() // pause
        assertNull(scheduledExpiry)
    }

    @Test
    fun `resuming schedules the expiry alarm for the time left`() = runBlocking {
        timerManager.toggle()
        clock += 10 * MINUTE
        timerManager.toggle() // pause
        clock += 3 * 60 * MINUTE // the user goes away for three hours

        timerManager.toggle() // resume
        assertEquals(clock + topic.focusTime - 10 * MINUTE, scheduledExpiry)
    }

    /** [NeverDispatcher] stands in for a suspended device, where the timer loop does not run */
    @Test
    fun `the expiry alarm ends the interval while the timer loop is not running`() = runBlocking {
        timerManager.toggle()
        clock += topic.focusTime + 1

        assertTrue(timerManager.expire(), "the alarm did not end the interval")
        assertEquals(TimerMode.SHORT_BREAK, stateRepository.timerState.value.timerMode)
        assertFalse(stateRepository.timerState.value.timerRunning)
        assertEquals(topic.focusTime, statRepository.focusTime)
        assertNull(scheduledExpiry)
    }

    @Test
    fun `an interval expires the moment its last millisecond is up`() = runBlocking {
        timerManager.toggle()
        clock += topic.focusTime

        assertTrue(timerManager.expire())
        assertEquals(TimerMode.SHORT_BREAK, stateRepository.timerState.value.timerMode)
    }

    @Test
    fun `an interval that is not over yet does not expire`() = runBlocking {
        timerManager.toggle()
        clock += MINUTE

        assertFalse(timerManager.expire())
        assertEquals(TimerMode.FOCUS, stateRepository.timerState.value.timerMode)
    }

    @Test
    fun `a stale expiry alarm left over from a pause does nothing`() = runBlocking {
        timerManager.toggle()
        clock += 5 * MINUTE
        timerManager.toggle() // pause
        clock += topic.focusTime // long enough that the interval would have been over

        assertFalse(timerManager.expire())
        assertEquals(TimerMode.FOCUS, stateRepository.timerState.value.timerMode)

        timerManager.saveTimeToDb()
        assertEquals(5 * MINUTE, statRepository.focusTime)
    }

    @Test
    fun `an interval cannot expire twice`() = runBlocking {
        timerManager.toggle()
        clock += topic.focusTime + 1

        assertTrue(timerManager.expire())
        assertFalse(timerManager.expire(), "the interval expired a second time")

        // A second advance would have moved on to the next focus interval
        assertEquals(TimerMode.SHORT_BREAK, stateRepository.timerState.value.timerMode)
        assertEquals(topic.focusTime, statRepository.focusTime)
        assertEquals(0L, statRepository.breakTime)
    }

    private fun TimerManager.toggle() = toggleTimer(
        scope = loopScope,
        onPause = {},
        onStart = {},
        onTick = { _, _, _ -> },
        onTimerExpired = {},
        onSkipComplete = {},
        setDoNotDisturb = {},
        onStateChanged = {}
    )

    private suspend fun TimerManager.skip() =
        skipTimer(onStart = {}, onCompletion = {}, setDoNotDisturb = {})

    private suspend fun TimerManager.expire() =
        expireIntervalIfDue(onTimerExpired = {}, onSkipComplete = {}, setDoNotDisturb = {})

    private companion object {
        const val MINUTE = 60_000L
        const val TIMEOUT = 5_000L
    }
}
