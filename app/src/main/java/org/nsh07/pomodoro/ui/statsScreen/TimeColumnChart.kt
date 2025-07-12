/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen

import android.graphics.Path
import android.graphics.RectF
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import org.nsh07.pomodoro.utils.millisecondsToHours

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TimeColumnChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    thickness: Dp = 40.dp,
    timeConverter: (Long) -> String = ::millisecondsToHours
) {
    val radius = with(LocalDensity.current) {
        (thickness / 2).toPx()
    }
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            vicoTheme.columnCartesianLayerColors.map { color ->
                                rememberLineComponent(
                                    fill = fill(color),
                                    thickness = thickness,
                                    shape = { _, path, left, top, right, bottom ->
                                        if (top + radius <= bottom - radius) {
                                            path.arcTo(
                                                RectF(left, top, right, top + 2 * radius),
                                                180f,
                                                180f
                                            )
                                            path.lineTo(right, bottom - radius)
                                            path.arcTo(
                                                RectF(left, bottom - 2 * radius, right, bottom),
                                                0f,
                                                180f
                                            )
                                            path.close()
                                        } else {
                                            path.addCircle(
                                                left + radius,
                                                bottom - radius,
                                                radius,
                                                Path.Direction.CW
                                            )
                                        }
                                    }
                                )
                            }
                        ),
                        columnCollectionSpacing = 4.dp
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = rememberLineComponent(Fill.Transparent),
                        tick = rememberLineComponent(Fill.Transparent),
                        guideline = rememberLineComponent(Fill.Transparent),
                        valueFormatter = CartesianValueFormatter { measuringContext, value, _ ->
                            timeConverter(value.toLong())
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        rememberLineComponent(Fill.Transparent),
                        tick = rememberLineComponent(Fill.Transparent),
                        guideline = rememberLineComponent(Fill.Transparent)
                    )
                ),
            modelProducer = modelProducer,
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = Zoom.fixed(),
                minZoom = Zoom.min(Zoom.Content, Zoom.fixed())
            ),
            modifier = modifier,
        )
    }
}