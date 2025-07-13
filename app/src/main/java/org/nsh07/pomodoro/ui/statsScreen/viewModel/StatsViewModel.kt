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
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.StatRepository
import java.time.format.TextStyle
import java.util.Locale

class StatsViewModel(
    statRepository: StatRepository
) : ViewModel() {

    val todayStat = statRepository.getTodayStat().distinctUntilChanged()
    private val lastWeekStatsSummary = statRepository.getLastNDaysStatsSummary(7)
    val lastWeekAverageFocusTimes =
        statRepository.getLastNDaysAverageFocusTimes(7).distinctUntilChanged()
    private val lastMonthStatsSummary = statRepository.getLastNDaysStatsSummary(30)
    val lastMonthAverageFocusTimes =
        statRepository.getLastNDaysAverageFocusTimes(30).distinctUntilChanged()

    val lastWeekSummaryChartData =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    val lastWeekSummaryAnalysisModelProducer = CartesianChartModelProducer()

    val lastMonthSummaryChartData =
        Pair(CartesianChartModelProducer(), ExtraStore.Key<List<String>>())
    val lastMonthSummaryAnalysisModelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            lastWeekStatsSummary
                .collect { list ->
                    // reversing is required because we need ascending order while the DB returns descending order
                    val reversed = list.reversed()
                    val keys = reversed.map {
                        it.date.dayOfWeek.getDisplayName(
                            TextStyle.NARROW,
                            Locale.getDefault()
                        )
                    }
                    val values = reversed.map { it.focusTime }
                    lastWeekSummaryChartData.first.runTransaction {
                        columnSeries { series(values) }
                        extras { it[lastWeekSummaryChartData.second] = keys }
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            lastWeekAverageFocusTimes
                .collect {
                    lastWeekSummaryAnalysisModelProducer.runTransaction {
                        columnSeries {
                            series(
                                it?.focusTimeQ1 ?: 0,
                                it?.focusTimeQ2 ?: 0,
                                it?.focusTimeQ3 ?: 0,
                                it?.focusTimeQ4 ?: 0
                            )
                        }
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            lastMonthStatsSummary
                .collect { list ->
                    val reversed = list.reversed()
                    val keys = reversed.map { it.date.dayOfMonth.toString() }
                    val values = reversed.map { it.focusTime }
                    lastMonthSummaryChartData.first.runTransaction {
                        columnSeries { series(values) }
                        extras { it[lastMonthSummaryChartData.second] = keys }
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            lastMonthAverageFocusTimes
                .collect {
                    lastMonthSummaryAnalysisModelProducer.runTransaction {
                        columnSeries {
                            series(
                                it?.focusTimeQ1 ?: 0,
                                it?.focusTimeQ2 ?: 0,
                                it?.focusTimeQ3 ?: 0,
                                it?.focusTimeQ4 ?: 0
                            )
                        }
                    }
                }
        }
    }

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