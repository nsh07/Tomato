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

package org.nsh07.pomodoro.timer

import dev.mccue.jlayer.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

/**
 * This class, well, plays MP3 files
 *
 * Note: please call [release] when you are done with an instance of this class
 *
 * @param audioPath The path to the MP3 file to play
 */
class MP3Player(val audioPath: String?) {

    private val audioFile = audioPath?.let { File(it) }
    private var player: Player? = null
    private var playJob: Job? = null

    private val audioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val isPlaying
        get() = playJob?.isActive == true

    /**
     * Starts looping playback until [stop] is called. If audio is already playing, it does nothing.
     */
    fun play() {
        if (isPlaying) return

        playJob = audioScope.launch {
            try {
                audioFile?.let {
                    while (isActive) {
                        val fileInputStream = FileInputStream(audioFile)
                        val bufferedInputStream = BufferedInputStream(fileInputStream)

                        player = Player(bufferedInputStream)
                        player?.play()
                    }
                }
            } catch (e: Exception) {
                println("Playback stopped or encountered an error: ${e.message}")
            } finally {
                reset()
            }
        }
    }

    /**
     * Stops playback and effectively seeks to 0
     */
    fun stop() {
        if (isPlaying) {
            playJob?.cancel()
            player?.close()
            reset()
        }
    }

    private fun reset() {
        player = null
        playJob = null
    }

    fun release() {
        stop()
        audioScope.cancel()
    }
}