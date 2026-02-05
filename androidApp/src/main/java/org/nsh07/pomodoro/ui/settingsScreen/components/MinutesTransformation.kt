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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.core.text.isDigitsOnly

class MinutesInputTransformation(val maxDigits: Int) : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!this.asCharSequence().isDigitsOnly() || this.length > maxDigits) {
            revertAllChanges()
        }
    }
}

val MinutesInputTransformation2Digits = MinutesInputTransformation(2)
val MinutesInputTransformation3Digits = MinutesInputTransformation(3)

/*
This OutputTransformation results in a crash with some keyboard apps, so I'm not using it right now
I might start using this later when this issue is resolved upstream in Compose

object MinutesOutputTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        when (this.length) {
            0 -> insert(0, "00")
            1 -> insert(0, "0")
        }
    }
}
*/