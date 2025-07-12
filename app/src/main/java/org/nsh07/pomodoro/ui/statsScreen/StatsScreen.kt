/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.runBlocking
import org.nsh07.pomodoro.ui.statsScreen.viewModel.StatsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle

@Composable
fun StatsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)
) {
    val allStatsSummaryModelProducer = viewModel.allStatsSummaryModelProducer
    StatsScreen(
        allStatsSummaryModelProducer = allStatsSummaryModelProducer,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    allStatsSummaryModelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        TopAppBar(
            title = {
                Text(
                    "Stats",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally
        )
        Text("This week", style = typography.headlineSmall, modifier = Modifier.padding(16.dp))
        TimeColumnChart(allStatsSummaryModelProducer, modifier = Modifier.padding(start = 16.dp))
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
            columnSeries { series(5, 6) }
        }
    }

    StatsScreen(
        allStatsSummaryModelProducer = modelProducer
    )
}
