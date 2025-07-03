package org.nsh07.pomodoro.ui

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.viewModel.UiViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    viewModel: UiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingTime by viewModel.time.collectAsStateWithLifecycle()

    val progress by rememberUpdatedState((uiState.totalTime.toFloat() - remainingTime) / uiState.totalTime)
    var showBrandTitle by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            delay(1500)
            showBrandTitle = false
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        bottomBar = {
            ShortNavigationBar {
                ShortNavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(painterResource(R.drawable.hourglass_filled), null) },
                    label = { Text("Timer") },
                    colors = ShortNavigationBarItemDefaults.colors()
                )
            }
        }
    ) { contentPadding ->
        TimerScreen(
            uiState = uiState,
            showBrandTitle = showBrandTitle,
            progress = { progress },
            resetTimer = viewModel::resetTimer,
            skipTimer = viewModel::skipTimer,
            toggleTimer = viewModel::toggleTimer,
            modifier = modifier.padding(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            )
        )
    }
}