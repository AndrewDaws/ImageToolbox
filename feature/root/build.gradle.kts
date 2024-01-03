@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

android {
    namespace = "ru.tech.imageresizershrinker.feature.root"
    compileSdk = libs.versions.androidCompileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toIntOrNull()
    }

    compileOptions {
        sourceCompatibility = rootProject.extra.get("javaCompile") as JavaVersion
        targetCompatibility = rootProject.extra.get("javaCompile") as JavaVersion
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    buildFeatures {
        compose = true
    }

    flavorDimensions += "app"

    productFlavors {
        create("foss") {
            dimension = "app"
        }
        create("market") {
            dimension = "app"
        }
        create("jxl") {
            dimension = "app"
        }
    }
}

dependencies {
    implementation(libs.hilt)
    kapt(libs.dagger.hilt.compiler)

    implementation(project(":core-domain"))
    implementation(project(":core-ui"))
    implementation(project(":core-data"))
    implementation(project(":core-resources"))

    implementation(project(":feature:load-net-image"))
    implementation(project(":feature:crop"))
    implementation(project(":feature:limits-resize"))
    implementation(project(":feature:cipher"))
    implementation(project(":feature:image-preview"))
    implementation(project(":feature:bytes-resize"))
    implementation(project(":feature:compare"))
    implementation(project(":feature:delete-exif"))
    implementation(project(":feature:generate-palette"))
    implementation(project(":feature:resize-convert"))
    implementation(project(":feature:pdf-tools"))
    implementation(project(":feature:single-edit"))
    implementation(project(":feature:erase-background"))
    implementation(project(":feature:draw"))
    implementation(project(":feature:filters"))
    implementation(project(":feature:image-stitch"))
    implementation(project(":feature:pick-color"))
    implementation(project(":feature:main"))
}