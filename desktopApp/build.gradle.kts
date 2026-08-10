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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.filekit.core)
}

compose.desktop {
    application {
        mainClass = "org.nsh07.pomodoro.MainKt"

        nativeDistributions {
            packageName = "Tomato"
            packageVersion = libs.versions.app.versionName.get()
            description = "Minimalist, data-oriented pomodoro timer"
            copyright = "Copyright (c) 2025-2026 Nishant Mishra"
            vendor = "Nishant Mishra"
            licenseFile.set(project.file("../LICENSE"))

            targetFormats(
                TargetFormat.AppImage,
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.Dmg,
                TargetFormat.Exe
            )

            val ogVersionName = libs.versions.app.versionName.get()
            val ogVersionCode = libs.versions.app.versionCode.get()

            linux {
                iconFile = project.file("../shared/src/jvmMain/composeResources/drawable/logo.png")
                shortcut = true
                menuGroup = "Utility;Clock;"
                appRelease = ogVersionCode
                appCategory = "TIMER"

                debMaintainer = "nishant.28@outlook.com"
                debPackageVersion = ogVersionName.replace('-', '~')

                rpmLicenseType = "GPLv3"
                rpmPackageVersion = getNativePackageVersion(ogVersionName, ogVersionCode)
            }
            macOS {
                iconFile = project.file("src/main/logo.icns")
                bundleID = "org.nsh07.pomodoro"
                appCategory = "public.app-category.productivity"
                packageVersion = getNativePackageVersion(ogVersionName, ogVersionCode)
            }
            windows {
                iconFile = project.file("src/main/logo.ico")
                description = "Tomato"
                console = false
                dirChooser = true
                perUserInstall = true
                shortcut = true
                packageVersion = getNativePackageVersion(ogVersionName, ogVersionCode)
            }
        }

        buildTypes.release.proguard {
            isEnabled = false
            optimize = true
        }
    }
}

/**
 * Converts a SemVer string to a native-packager-friendly integer sequence.
 *
 * The patch component is replaced by the version code, which increments on every release and so
 * keeps pre-releases below the release they lead up to.
 *
 * Example:
 *
 * ```
 * "1.8.6"         with version code 34 -> "1.8.34"
 * "2.0.0-alpha01" with version code 36 -> "2.0.36"
 * ```
 */
fun getNativePackageVersion(semanticVersion: String, versionCode: String): String {
    val regex = """^(\d+)\.(\d+)\.(\d+)(?:-([a-zA-Z]+)(\d+))?$""".toRegex()

    val match = regex.matchEntire(semanticVersion)
        ?: throw IllegalArgumentException("Version string does not match the expected format: $semanticVersion")

    val (major, minor) = match.destructured

    return "$major.$minor.$versionCode"
}