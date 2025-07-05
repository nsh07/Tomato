package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.core.text.isDigitsOnly

object MinutesInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!this.asCharSequence().isDigitsOnly() || this.length > 2) {
            revertAllChanges()
        }
    }
}