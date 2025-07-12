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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.nsh07.pomodoro.TomatoApplication
import org.nsh07.pomodoro.data.StatRepository

class StatsViewModel(
    statRepository: StatRepository
) : ViewModel() {
    val todayStat = statRepository.getTodayStat().distinctUntilChanged()
    private val allStatsSummary = statRepository.getLastWeekStatsSummary()
    private val averageFocusTimes = statRepository.getAverageFocusTimes()

    val allStatsSummaryModelProducer = CartesianChartModelProducer()
    val todayStatModelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allStatsSummary
                .collect { list ->
                    allStatsSummaryModelProducer.runTransaction {
                        columnSeries { series(list.reversed().map { it.focusTime }) }
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            todayStat
                .collect {
                    todayStatModelProducer.runTransaction {
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