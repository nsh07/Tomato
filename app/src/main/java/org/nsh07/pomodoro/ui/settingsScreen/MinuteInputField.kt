package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinuteInputField(
    state: TextFieldState,
    shape: Shape,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    BasicTextField(
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = MinutesInputTransformation,
        outputTransformation = MinutesOutputTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        textStyle = TextStyle(
            fontFamily = openRundeClock,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp,
            letterSpacing = (-2).sp,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(colorScheme.onSurface),
        decorator = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(112.dp, 100.dp)
                    .background(
                        animateColorAsState(
                            if (state.text.isNotEmpty())
                                listItemColors.containerColor
                            else colorScheme.errorContainer,
                            motionScheme.defaultEffectsSpec()
                        ).value,
                        shape
                    )
            ) { innerTextField() }
        }
    )
}