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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.utils.millisecondsToHoursMinutes

@Composable
fun ColumnScope.ProductivityGraph(
    expanded: Boolean,
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.productivity_analysis)
) {
    AnimatedVisibility(expanded) {
        Column(modifier = modifier) {
            Text(label, style = typography.titleMedium)
            Text(
                stringResource(R.string.productivity_analysis_desc),
                style = typography.bodySmall
            )
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
                }
            )
        }
    }
}