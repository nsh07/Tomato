/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.runBlocking
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.data.Stat
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@Composable
fun StatsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val todayStat by viewModel.todayStat.collectAsState(null)
    StatsScreen(
        lastWeekSummaryModelProducer = viewModel.lastWeekSummaryChartModelProducer,
        todayStatModelProducer = viewModel.todayStatModelProducer,
        todayStat = todayStat,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    lastWeekSummaryModelProducer: CartesianChartModelProducer,
    todayStatModelProducer: CartesianChartModelProducer,
    todayStat: Stat?,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var todayStatExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Stats",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTopBar,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text(
                    "Today",
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.primaryContainer,
                                MaterialTheme.shapes.largeIncreased
                            )
                            .weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Focus",
                                style = typography.titleMedium,
                                color = colorScheme.onPrimaryContainer
                            )
                            Text(
                                if (todayStat != null) remember(todayStat) {
                                    millisecondsToHoursMinutes(
                                        todayStat.focusTimeQ1 +
                                                todayStat.focusTimeQ2 +
                                                todayStat.focusTimeQ3 +
                                                todayStat.focusTimeQ4
                                    )
                                } else "0h 0m",
                                style = typography.displaySmall,
                                fontFamily = openRundeClock,
                                color = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                colorScheme.tertiaryContainer,
                                MaterialTheme.shapes.largeIncreased
                            )
                            .weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Break",
                                style = typography.titleMedium,
                                color = colorScheme.onTertiaryContainer
                            )
                            Text(
                                if (todayStat != null) remember(todayStat) {
                                    millisecondsToHoursMinutes(todayStat.breakTime)
                                } else "0h 0m",
                                style = typography.displaySmall,
                                fontFamily = openRundeClock,
                                color = colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
            item {
                val iconRotation by animateFloatAsState(
                    if (todayStatExpanded) 180f else 0f,
                    animationSpec = motionScheme.defaultSpatialSpec()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalIconToggleButton(
                        checked = todayStatExpanded,
                        onCheckedChange = { todayStatExpanded = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .width(52.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_down),
                            "More info",
                            modifier = Modifier.rotate(iconRotation)
                        )
                    }
                    ProductivityGraph(
                        todayStatExpanded,
                        todayStatModelProducer,
                        Modifier.padding(horizontal = 32.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                Text(
                    "This week",
                    style = typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            item {
                TimeColumnChart(
                    lastWeekSummaryModelProducer,
                    modifier = Modifier.padding(start = 16.dp)
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
fun StatsScreenPreview() {
    val modelProducer = remember { CartesianChartModelProducer() }

    runBlocking {
        modelProducer.runTransaction {
            columnSeries {
                series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7)
            }
        }
    }

    StatsScreen(
        modelProducer,
        modelProducer,
        null
    )
}
