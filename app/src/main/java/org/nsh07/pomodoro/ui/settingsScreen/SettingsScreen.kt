package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle
import org.nsh07.pomodoro.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    focusTime: Int,
    shortBreakTime: Int,
    longBreakTime: Int,
    updateFocusTime: (Int) -> Unit,
    updateShortBreakTime: (Int) -> Unit,
    updateLongBreakTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val focusTimeInputFieldState = rememberTextFieldState(
        (focusTime / 60000).toString().padStart(2, '0')
    )
    val shortBreakTimeInputFieldState = rememberTextFieldState(
        (shortBreakTime / 60000).toString().padStart(2, '0')
    )
    val longBreakTimeInputFieldState = rememberTextFieldState(
        (longBreakTime / 60000).toString().padStart(2, '0')
    )

    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    "Durations",
                    style = typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(112.dp, 100.dp)
                                .background(
                                    animateColorAsState(
                                        if (focusTimeInputFieldState.text.isNotEmpty())
                                            colorScheme.surfaceContainer
                                        else colorScheme.errorContainer,
                                        motionScheme.defaultEffectsSpec()
                                    ).value,
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        bottomStart = 16.dp,
                                        topEnd = 4.dp,
                                        bottomEnd = 4.dp
                                    )
                                )
                        ) {
                            BasicTextField(
                                state = focusTimeInputFieldState,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                inputTransformation = MinutesInputTransformation,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                textStyle = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp,
                                    color = colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(colorScheme.onSurface)
                            )
                        }
                        Text(
                            "Focus",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(112.dp, 100.dp)
                                .background(
                                    animateColorAsState(
                                        if (shortBreakTimeInputFieldState.text.isNotEmpty())
                                            colorScheme.surfaceContainer
                                        else colorScheme.errorContainer,
                                        motionScheme.defaultEffectsSpec()
                                    ).value,
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            BasicTextField(
                                state = shortBreakTimeInputFieldState,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                inputTransformation = MinutesInputTransformation,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                textStyle = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp,
                                    color = colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(colorScheme.onSurface)
                            )
                        }
                        Text(
                            "Short break",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(112.dp, 100.dp)
                                .background(
                                    animateColorAsState(
                                        if (longBreakTimeInputFieldState.text.isNotEmpty())
                                            colorScheme.surfaceContainer
                                        else colorScheme.errorContainer,
                                        motionScheme.defaultEffectsSpec()
                                    ).value,
                                     RoundedCornerShape(
                                        topStart = 4.dp,
                                        bottomStart = 4.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp
                                    )
                                )
                        ) {
                            BasicTextField(
                                state = longBreakTimeInputFieldState,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                inputTransformation = MinutesInputTransformation,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                textStyle = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp,
                                    color = colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(colorScheme.onSurface)
                            )
                        }
                        Text(
                            "Long break",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun SettingsScreenPreview() {
    TomatoTheme {
        SettingsScreen(
            focusTime = 25 * 60 * 1000,
            shortBreakTime = 5 * 60 * 1000,
            longBreakTime = 15 * 60 * 1000,
            updateFocusTime = {},
            updateShortBreakTime = {},
            updateLongBreakTime = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
