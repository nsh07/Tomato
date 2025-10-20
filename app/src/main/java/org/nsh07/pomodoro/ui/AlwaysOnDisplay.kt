/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui

import android.app.Activity
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.coroutines.delay
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.AlwaysOnDisplay(
    timerState: TimerState,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    var sharedElementTransitionComplete by remember { mutableStateOf(false) }

    val view = LocalView.current
    val window = remember { (view.context as Activity).window }
    val insetsController = remember { WindowCompat.getInsetsController(window, view) }

    DisposableEffect(Unit) {
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        sharedElementTransitionComplete = true
    }

    val primary by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFFA2A2A2)
        else {
            if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary
            else colorScheme.tertiary
        },
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val secondaryContainer by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFF1D1D1D)
        else {
            if (timerState.timerMode == TimerMode.FOCUS) colorScheme.secondaryContainer
            else colorScheme.tertiaryContainer
        },
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val surface by animateColorAsState(
        if (sharedElementTransitionComplete) Color.Black
        else colorScheme.surface,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onSurface by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFFE3E3E3)
        else colorScheme.onSurface,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        if (timerState.timerMode == TimerMode.FOCUS) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("focus progress"),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current
                    )
                    .size(250.dp),
                color = primary,
                trackColor = secondaryContainer,
                strokeWidth = 12.dp,
                gapSize = 8.dp,
            )
        } else {
            CircularWavyProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("break progress"),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current
                    )
                    .size(250.dp),
                color = primary,
                trackColor = secondaryContainer,
                stroke = Stroke(
                    width = with(LocalDensity.current) {
                        12.dp.toPx()
                    },
                    cap = StrokeCap.Round,
                ),
                trackStroke = Stroke(
                    width = with(LocalDensity.current) {
                        12.dp.toPx()
                    },
                    cap = StrokeCap.Round,
                ),
                wavelength = 42.dp,
                gapSize = 8.dp
            )
        }

        Text(
            text = timerState.timeStr,
            style = TextStyle(
                fontFamily = openRundeClock,
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp,
                letterSpacing = (-2).sp
            ),
            textAlign = TextAlign.Center,
            color = onSurface,
            maxLines = 1,
            modifier = Modifier.sharedBounds(
                sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("clock"),
                animatedVisibilityScope = LocalNavAnimatedContentScope.current
            )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun AlwaysOnDisplayPreview() {
    val timerState = TimerState()
    val progress = { 0.5f }
    TomatoTheme {
        SharedTransitionLayout {
            AlwaysOnDisplay(
                timerState = timerState,
                progress = progress
            )
        }
    }
}
