/*
 * Copyright (c) 2025-2026 Nishant Mishra
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

package org.nsh07.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.textAsString
import androidx.test.uiautomator.uiAutomator
import androidx.test.uiautomator.watcher.PermissionDialog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: throw Exception("targetAppId not passed as instrumentation runner arg")
        rule.collect(
            packageName = packageName,
            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()

            uiAutomator {
                onElement { contentDescription == "Play" }.click()
                watchFor(PermissionDialog) {
                    clickAllow() // Allow notification permission
                }

                waitForAppToBeVisible(packageName)

                onElement { contentDescription == "Restart" }.click()

                onElement { contentDescription == "Stats" }.click()
                waitForStableInActiveWindow()
                scrollThroughContent()

                onElement { textAsString() == "This week" }.click()
                waitForStableInActiveWindow()
                scrollThroughContent()
                onElement { contentDescription == "Back" }.click()

                waitForStableInActiveWindow()

                onElement { textAsString() == "This month" }.click()
                waitForStableInActiveWindow()
                scrollThroughContent()
                onElement { contentDescription == "Back" }.click()

                waitForStableInActiveWindow()

                fling(500, 1500, "UP")
                waitForStableInActiveWindow()

                onElement { textAsString() == "This year" }.click()
                waitForStableInActiveWindow()
                scrollThroughContent()
                onElement { contentDescription == "Back" }.click()

                waitForStableInActiveWindow()

                onElement { contentDescription == "Settings" }.click()
                waitForStableInActiveWindow()

                pressBack()
            }
        }
    }

    private fun UiAutomatorTestScope.fling(startX: Int, startY: Int, direction: String) {
        val screenHeight = device.displayHeight
        val screenWidth = device.displayWidth
        val steps = 5 // Fast speed for fling

        var endX = startX
        var endY = startY

        when (direction.uppercase(Locale.getDefault())) {
            "UP" -> endY = startY - (screenHeight * 0.3).toInt()
            "DOWN" -> endY = startY + (screenHeight * 0.3).toInt()
            "LEFT" -> endX = startX - (screenWidth * 0.3).toInt()
            "RIGHT" -> endX = startX + (screenWidth * 0.3).toInt()
        }

        device.swipe(startX, startY, endX, endY, steps)
    }

    private fun UiAutomatorTestScope.scrollThroughContent() {
        fling(500, 1500, "UP")
        waitForStableInActiveWindow()

        fling(500, 1500, "DOWN")
        waitForStableInActiveWindow()
    }
}