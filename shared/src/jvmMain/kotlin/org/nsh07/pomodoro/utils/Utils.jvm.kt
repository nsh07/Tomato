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

package org.nsh07.pomodoro.utils

actual fun androidSdkVersionAtLeast(version: Int): Boolean = false

actual fun androidDeviceManufacturerIs(manufacturer: String): Boolean {
    TODO("Not yet implemented")
}

actual fun getDefaultAlarmTone(): String? {
    // TODO: implement
    return null
}

actual fun logError(tag: String, message: String): Int {
    TODO("Not yet implemented")
}