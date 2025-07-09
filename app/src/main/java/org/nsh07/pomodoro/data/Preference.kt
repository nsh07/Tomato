package org.nsh07.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "int_preference")
data class IntPreference(
    @PrimaryKey
    val key: String,
    val value: Int
)
