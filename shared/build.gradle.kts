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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
}

// TODO: remove when CMP migration is done
compose.resources {
    publicResClass = true
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    android {
        namespace = "org.nsh07.pomodoro.shared"
        compileSdk = 36
        minSdk = 26

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.components.resources)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.adaptive)

            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.compose.adaptive.navigation3)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)

            implementation(libs.vico.compose.m3)
            implementation(libs.material.kolor)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))

            // Android-specific Compose
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)

            implementation(libs.koin.android)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.ui.test.junit4)
            implementation(libs.androidx.ui.test.manifest)
        }
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

androidComponents {
    onVariants { variant ->
        variant.sources.res?.addStaticSourceDirectory("src/commonMain/composeResources")
    }
}