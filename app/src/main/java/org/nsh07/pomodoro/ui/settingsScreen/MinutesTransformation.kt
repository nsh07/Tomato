package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.core.text.isDigitsOnly

object MinutesInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (!this.asCharSequence().isDigitsOnly() || this.length > 2) {
            revertAllChanges()
        }
    }
}

object MinutesOutputTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        if (this.length == 0) {
            insert(0, "00")
        } else if (this.toString().toInt() < 10) {
            insert(0, "0")
        }
    }
}