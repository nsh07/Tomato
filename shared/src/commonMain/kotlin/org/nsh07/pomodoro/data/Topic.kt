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

package org.nsh07.pomodoro.data

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.graphics.shapes.RoundedPolygon
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a Topic with its own timer settings. Names are unique, case-insensitively.
 */
@Entity(
    tableName = "topic",
    indices = [Index(value = ["name"], unique = true)]
)
data class Topic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val color: Color,
    val shape: TopicShape,
    val focusTime: Long,
    val shortBreakTime: Long,
    val longBreakTime: Long,
    val sessionLength: Int,
    val autostartNextSession: Boolean,
    val dndEnabled: Boolean
) {
    companion object {
        /** Row id of the topic seeded when the database is created. */
        const val DEFAULT_TOPIC_ID = 1L

        val defaultTopic = Topic(
            id = DEFAULT_TOPIC_ID,
            name = "Default",
            color = Color.White,
            shape = TopicShape.COOKIE_12_SIDED,
            focusTime = 25 * 60 * 1000L,
            shortBreakTime = 5 * 60 * 1000L,
            longBreakTime = 15 * 60 * 1000L,
            sessionLength = 4,
            autostartNextSession = false,
            dndEnabled = false
        )
    }
}

enum class TopicShape {
    CIRCLE, SQUARE, SLANTED, ARCH, FAN, ARROW, SEMI_CIRCLE, OVAL, PILL, TRIANGLE, DIAMOND,
    CLAM_SHELL, PENTAGON, GEM, SUNNY, VERY_SUNNY, COOKIE_4_SIDED, COOKIE_6_SIDED, COOKIE_7_SIDED,
    COOKIE_9_SIDED, COOKIE_12_SIDED, GHOSTISH, CLOVER_4_LEAF, CLOVER_8_LEAF, BURST, SOFT_BURST,
    BOOM, SOFT_BOOM, FLOWER, PUFFY, PUFFY_DIAMOND, PIXEL_CIRCLE, PIXEL_TRIANGLE, BUN, HEART;

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun toRoundedPolygon(): RoundedPolygon {
        return when (this) {
            CIRCLE -> MaterialShapes.Circle
            SQUARE -> MaterialShapes.Square
            SLANTED -> MaterialShapes.Slanted
            ARCH -> MaterialShapes.Arch
            FAN -> MaterialShapes.Fan
            ARROW -> MaterialShapes.Arrow
            SEMI_CIRCLE -> MaterialShapes.SemiCircle
            OVAL -> MaterialShapes.Oval
            PILL -> MaterialShapes.Pill
            TRIANGLE -> MaterialShapes.Triangle
            DIAMOND -> MaterialShapes.Diamond
            CLAM_SHELL -> MaterialShapes.ClamShell
            PENTAGON -> MaterialShapes.Pentagon
            GEM -> MaterialShapes.Gem
            SUNNY -> MaterialShapes.Sunny
            VERY_SUNNY -> MaterialShapes.VerySunny
            COOKIE_4_SIDED -> MaterialShapes.Cookie4Sided
            COOKIE_6_SIDED -> MaterialShapes.Cookie6Sided
            COOKIE_7_SIDED -> MaterialShapes.Cookie7Sided
            COOKIE_9_SIDED -> MaterialShapes.Cookie9Sided
            COOKIE_12_SIDED -> MaterialShapes.Cookie12Sided
            GHOSTISH -> MaterialShapes.Ghostish
            CLOVER_4_LEAF -> MaterialShapes.Clover4Leaf
            CLOVER_8_LEAF -> MaterialShapes.Clover8Leaf
            BURST -> MaterialShapes.Burst
            SOFT_BURST -> MaterialShapes.SoftBurst
            BOOM -> MaterialShapes.Boom
            SOFT_BOOM -> MaterialShapes.SoftBoom
            FLOWER -> MaterialShapes.Flower
            PUFFY -> MaterialShapes.Puffy
            PUFFY_DIAMOND -> MaterialShapes.PuffyDiamond
            PIXEL_CIRCLE -> MaterialShapes.PixelCircle
            PIXEL_TRIANGLE -> MaterialShapes.PixelTriangle
            BUN -> MaterialShapes.Bun
            HEART -> MaterialShapes.Heart
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun toShape(startAngle: Int = 0) = this.toRoundedPolygon().toShape(startAngle)
}
