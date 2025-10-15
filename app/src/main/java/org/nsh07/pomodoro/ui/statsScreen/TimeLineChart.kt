/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineFill.Companion.single
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.millisecondsToHours
import org.nsh07.pomodoro.utils.millisecondsToMinutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeLineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    thickness: Float = 2f,
    pointSpacing: Dp = 12.dp,
    xValueFormatter: CartesianValueFormatter = CartesianValueFormatter.Default,
    yValueFormatter: CartesianValueFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 60 * 60 * 1000) {
            millisecondsToHours(value.toLong())
        } else {
            millisecondsToMinutes(value.toLong())
        }
    },
    animationSpec: AnimationSpec<Float>? = motionScheme.slowEffectsSpec()
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        LineCartesianLayer.LineProvider.series(
                            vicoTheme.lineCartesianLayerColors.map { color ->
                                LineCartesianLayer.rememberLine(
                                    fill = single(fill(color)),
                                    stroke = LineCartesianLayer.LineStroke.Continuous(
                                        thicknessDp = thickness,
                                    ),
                                    areaFill = LineCartesianLayer.AreaFill.single(
                                        fill(
                                            ShaderProvider.verticalGradient(
                                                color.toArgb(),
                                                Color.Transparent.toArgb()
                                            )
                                        )
                                    ),
                                    pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f)
                                )
                            }
                        ),
                        pointSpacing = pointSpacing
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = rememberLineComponent(Fill.Transparent),
                        tick = rememberLineComponent(Fill.Transparent),
                        guideline = rememberLineComponent(Fill.Transparent),
                        valueFormatter = yValueFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        rememberLineComponent(Fill.Transparent),
                        tick = rememberLineComponent(Fill.Transparent),
                        guideline = rememberLineComponent(Fill.Transparent),
                        valueFormatter = xValueFormatter
                    ),
                    fadingEdges = FadingEdges()
                ),
            modelProducer = modelProducer,
            zoomState = rememberVicoZoomState(
                zoomEnabled = true,
                initialZoom = Zoom.fixed(),
                minZoom = Zoom.min(Zoom.Content, Zoom.fixed())
            ),
            animationSpec = animationSpec,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun TimeLineChartPreview() {
    val modelProducer = remember { CartesianChartModelProducer() }
    val values = mutableListOf<Int>()
    LaunchedEffect(Unit) {
        repeat(365) {
            values.add((0..120).random() * 60 * 1000)
        }
        modelProducer.runTransaction {
            lineSeries {
                series(values)
            }
        }
    }
    TomatoTheme {
        Surface {
            TimeLineChart(modelProducer = modelProducer)
        }
    }
}
