/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@Composable
fun ColumnScope.ProductivityGraph(
    expanded: Boolean,
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    label: String = "Productivity analysis"
) {
    AnimatedVisibility(expanded) {
        Column(modifier = modifier) {
            Text(label, style = typography.titleMedium)
            Text("Time of day versus focus hours", style = typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            TimeColumnChart(
                modelProducer,
                xValueFormatter = CartesianValueFormatter { _, value, _ ->
                    when (value) {
                        0.0 -> "0 - 6"
                        1.0 -> "6 - 12"
                        2.0 -> "12 - 18"
                        3.0 -> "18 - 24"
                        else -> ""
                    }
                },
                yValueFormatter = CartesianValueFormatter { _, value, _ ->
                    millisecondsToHoursMinutes(value.toLong())
                },
                animationSpec = null
            )
        }
    }
}