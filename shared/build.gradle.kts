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

kotlin {
    androidLibrary {
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
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
            implementation(libs.androidx.ui.tooling.preview)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.adaptive)
            implementation(libs.androidx.compose.adaptive.navigation3)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        androidMain.dependencies {
            // Android-specific Compose
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            // Keep using AndroidX Compose for now (you can migrate to CMP later)
            implementation(libs.androidx.core.ktx)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))

            implementation(libs.vico.compose.m3)
            implementation(libs.material.kolor)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)

            // Glance widgets
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)

            implementation(libs.koin.android)
        }

        androidUnitTest.dependencies {
            implementation(libs.junit)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.ui.test.junit4)
        }
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}