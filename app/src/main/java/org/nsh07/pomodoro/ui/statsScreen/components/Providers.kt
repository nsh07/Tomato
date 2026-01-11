/*
 * Copyright (c) 2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.statsScreen.components

import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.ColumnProvider
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore

class Providers {
    companion object {
        private data class ColumnProviderWithLimit(
            private val limit: Double,
            private val belowLimitComponent: LineComponent,
            private val aboveLimitComponent: LineComponent
        ) : ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent {
                return if (entry.y < limit) belowLimitComponent
                else aboveLimitComponent
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent {
                return if (belowLimitComponent.thicknessDp > aboveLimitComponent.thicknessDp) belowLimitComponent
                else aboveLimitComponent
            }
        }

        fun columnProviderWithLimit(
            limit: Long,
            belowLimitComponent: LineComponent,
            aboveLimitComponent: LineComponent
        ): ColumnProvider =
            ColumnProviderWithLimit(limit.toDouble(), belowLimitComponent, aboveLimitComponent)
    }
}
