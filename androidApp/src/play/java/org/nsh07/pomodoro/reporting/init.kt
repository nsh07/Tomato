package org.nsh07.pomodoro.reporting

import android.app.Application
import org.nsh07.pomodoro.BuildConfig
import sh.measure.android.Measure
import sh.measure.android.config.MeasureConfig

fun Application.initMeasureReporting() =
    Measure.init(this, MeasureConfig(enableLogging = BuildConfig.DEBUG))