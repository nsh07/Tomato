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

            linux {
                iconFile = project.file("src/main/resources/drawable/logo.png")
                debMaintainer = "nishant.28@outlook.com"
                appRelease = libs.versions.app.versionCode.get()
                appCategory = "TIMER"
                rpmLicenseType = "GPLv3"
            }
            macOS {
                iconFile = project.file("src/main/resources/drawable/logo.icns")
                bundleID = "org.nsh07.pomodoro"
                appCategory = "public.app-category.productivity"
            }
            windows {
                iconFile = project.file("src/main/resources/drawable/logo.ico")
                console = false
                dirChooser = true
                perUserInstall = true
                shortcut = true
            }
        }

        buildTypes.release.proguard {
            isEnabled = false
            optimize = true
        }
    }
}