/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.timerScreen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerScreen(
    timerState: TimerState,
    progress: () -> Float,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme
    val haptic = LocalHapticFeedback.current

    val color by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary
        else colorScheme.tertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onColor by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.onPrimary
        else colorScheme.onTertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val colorContainer by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.secondaryContainer
        else colorScheme.tertiaryContainer,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                AnimatedContent(
                    if (!timerState.showBrandTitle) timerState.timerMode else TimerMode.BRAND,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetY = { (-it * 1.25).toInt() }
                        ).togetherWith(
                            slideOutVertically(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetY = { (it * 1.25).toInt() }
                            )
                        )
                    }
                ) {
                    when (it) {
                        TimerMode.BRAND ->
                            Text(
                                "Tomato",
                                style = TextStyle(
                                    fontFamily = robotoFlexTopBar,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.onErrorContainer
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(210.dp)
                            )

                        TimerMode.FOCUS ->
                            Text(
                                "Focus",
                                style = TextStyle(
                                    fontFamily = robotoFlexTopBar,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.onPrimaryContainer
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(210.dp)
                            )

                        TimerMode.SHORT_BREAK -> Text(
                            "Short break",
                            style = TextStyle(
                                fontFamily = robotoFlexTopBar,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.onTertiaryContainer
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(210.dp)
                        )

                        TimerMode.LONG_BREAK -> Text(
                            "Long Break",
                            style = TextStyle(
                                fontFamily = robotoFlexTopBar,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.onTertiaryContainer
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(210.dp)
                        )
                    }
                }
            },
            subtitle = {},
            titleHorizontalAlignment = CenterHorizontally
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    if (timerState.timerMode == TimerMode.FOCUS) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            strokeWidth = 16.dp,
                            gapSize = 8.dp
                        )
                    } else {
                        CircularWavyProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            stroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            trackStroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            wavelength = 60.dp,
                            gapSize = 8.dp
                        )
                    }
                    var expanded by remember { mutableStateOf(timerState.showBrandTitle) }
                    Column(
                        horizontalAlignment = CenterHorizontally,
                        modifier = Modifier
                            .clip(shapes.largeIncreased)
                            .clickable(onClick = { expanded = !expanded })
                    ) {
                        LaunchedEffect(timerState.showBrandTitle) {
                            expanded = timerState.showBrandTitle
                        }
                        Text(
                            text = timerState.timeStr,
                            style = TextStyle(
                                fontFamily = openRundeClock,
                                fontWeight = FontWeight.Bold,
                                fontSize = 72.sp,
                                letterSpacing = (-2).sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        AnimatedVisibility(
                            expanded,
                            enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                                    expandVertically(motionScheme.defaultSpatialSpec()),
                            exit = fadeOut(motionScheme.defaultEffectsSpec()) +
                                    shrinkVertically(motionScheme.defaultSpatialSpec())
                        ) {
                            Text(
                                "${timerState.currentFocusCount} of ${timerState.totalFocusCount}",
                                fontFamily = openRundeClock,
                                style = typography.titleLarge,
                                color = colorScheme.outline
                            )
                        }
                    }
                }
                val interactionSources = remember { List(3) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = { state ->
                        FilledTonalIconButton(
                            onClick = {
                                if (state.isExpanded) {
                                    state.dismiss()
                                } else {
                                    state.show()
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colorContainer
                            ),
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier
                                .size(64.dp, 96.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.more_vert_large),
                                contentDescription = "More",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    customItem(
                        {
                            FilledIconToggleButton(
                                onCheckedChange = { checked ->
                                    onAction(TimerAction.ToggleTimer)

                                    if (checked) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    else haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checked) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                checked = timerState.timerRunning,
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = color,
                                    checkedContentColor = onColor
                                ),
                                shapes = IconButtonDefaults.toggleableShapes(),
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(width = 128.dp, height = 96.dp)
                                    .animateWidth(interactionSources[0])
                            ) {
                                if (timerState.timerRunning) {
                                    Icon(
                                        painterResource(R.drawable.pause_large),
                                        contentDescription = "Pause",
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        painterResource(R.drawable.play_large),
                                        contentDescription = "Play",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (timerState.timerRunning) {
                                        Icon(
                                            painterResource(R.drawable.pause),
                                            contentDescription = "Pause"
                                        )
                                    } else {
                                        Icon(
                                            painterResource(R.drawable.play),
                                            contentDescription = "Play"
                                        )
                                    }
                                },
                                text = { Text(if (timerState.timerRunning) "Pause" else "Play") },
                                onClick = {
                                    onAction(TimerAction.ToggleTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(96.dp)
                                    .animateWidth(interactionSources[1])
                            ) {
                                Icon(
                                    painterResource(R.drawable.restart_large),
                                    contentDescription = "Restart",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.restart),
                                        "Restart"
                                    )
                                },
                                text = { Text("Restart") },
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(64.dp, 96.dp)
                                    .animateWidth(interactionSources[2])
                            ) {
                                Icon(
                                    painterResource(R.drawable.skip_next_large),
                                    contentDescription = "Skip to next",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.skip_next),
                                        "Skip to next"
                                    )
                                },
                                text = { Text("Skip to next") },
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    state.dismiss()
                                }
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = CenterHorizontally) {
                Text("Up next", style = typography.titleSmall)
                Text(
                    timerState.nextTimeStr,
                    style = TextStyle(
                        fontFamily = openRundeClock,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        color = if (timerState.nextTimerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary
                    )
                )
                Text(
                    when (timerState.nextTimerMode) {
                        TimerMode.FOCUS -> "Focus"
                        TimerMode.SHORT_BREAK -> "Short break"
                        else -> "Long Break"
                    },
                    style = typography.titleMediumEmphasized
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TimerScreenPreview() {
    val timerState = TimerState(
        timeStr = "03:34", nextTimeStr = "5:00", timerMode = TimerMode.FOCUS, timerRunning = true
    )
    TomatoTheme {
        TimerScreen(
            timerState,
            { 0.3f },
            {}
        )
    }
}
