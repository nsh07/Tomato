/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.StatRepository
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class StatsViewModel(
    statRepository: StatRepository
) : ViewModel() {

    val todayStat = statRepository.getTodayStat().distinctUntilChanged()

    private val lastWeekSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    private val lastMonthSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    private val lastYearSummary =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())

    private val yearDayFormatter = DateTimeFormatter.ofPattern("d MMM")

    val lastWeekSummaryChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        statRepository.getLastNDaysStatsSummary(7)
            .map { list ->
                // reversing is required because we need ascending order while the DB returns descending order
                val reversed = list.reversed()
                val keys = reversed.map {
                    it.date.dayOfWeek.getDisplayName(
                        TextStyle.NARROW,
                        Locale.getDefault()
                    )
                }
                val values = reversed.map { it.focusTime }
                lastWeekSummary.first.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastWeekSummary.second] = keys }
                }
                lastWeekSummary
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastWeekSummary
            )

    val lastWeekAverageFocusTimes: StateFlow<List<Int>> =
        statRepository.getLastNDaysAverageFocusTimes(7)
            .map {
                listOf(
                    it?.focusTimeQ1?.toInt() ?: 0,
                    it?.focusTimeQ2?.toInt() ?: 0,
                    it?.focusTimeQ3?.toInt() ?: 0,
                    it?.focusTimeQ4?.toInt() ?: 0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf(0, 0, 0, 0)
            )

    val lastMonthSummaryChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        statRepository.getLastNDaysStatsSummary(30)
            .map { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.dayOfMonth.toString() }
                val values = reversed.map { it.focusTime }
                lastMonthSummary.first.runTransaction {
                    columnSeries { series(values) }
                    extras { it[lastMonthSummary.second] = keys }
                }
                lastMonthSummary
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastMonthSummary
            )

    val lastMonthAverageFocusTimes: StateFlow<List<Int>> =
        statRepository.getLastNDaysAverageFocusTimes(30)
            .map {
                listOf(
                    it?.focusTimeQ1?.toInt() ?: 0,
                    it?.focusTimeQ2?.toInt() ?: 0,
                    it?.focusTimeQ3?.toInt() ?: 0,
                    it?.focusTimeQ4?.toInt() ?: 0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf(0, 0, 0, 0)
            )

    val lastYearSummaryChartData: StateFlow<Pair<CartesianChartModelProducer, ExtraStore.Key<List<String>>>> =
        statRepository.getLastNDaysStatsSummary(365)
            .map { list ->
                val reversed = list.reversed()
                val keys = reversed.map { it.date.format(yearDayFormatter) }
                val values = reversed.map { it.focusTime }
                lastYearSummary.first.runTransaction {
                    lineSeries { series(values) }
                    extras { it[lastYearSummary.second] = keys }
                }
                lastYearSummary
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = lastYearSummary
            )

    val lastYearAverageFocusTimes: StateFlow<List<Int>> =
        statRepository.getLastNDaysAverageFocusTimes(365)
            .map {
                listOf(
                    it?.focusTimeQ1?.toInt() ?: 0,
                    it?.focusTimeQ2?.toInt() ?: 0,
                    it?.focusTimeQ3?.toInt() ?: 0,
                    it?.focusTimeQ4?.toInt() ?: 0
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = listOf(0, 0, 0, 0)
            )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TomatoApplication)
                val appStatRepository = application.container.appStatRepository

                StatsViewModel(appStatRepository)
            }
        }
    }
}