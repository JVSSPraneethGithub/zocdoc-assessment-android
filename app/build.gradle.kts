plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.zocdoc.assessment"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zocdoc.assessment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "AUTH_TOKEN", "\"3b502b3f-b1ff-4128-bd99-626e74836d9c\"")

        sourceSets {
            getByName("test") {
                resources.srcDirs("src/shared-test-resources")
            }
            getByName("androidTest") {
                resources.srcDirs("src/shared-test-resources")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    // Dependencies licenses issues fix
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
}

// Hamcrest matchers fix
configurations.all {
    resolutionStrategy {
        force("org.hamcrest:hamcrest-library:1.3")
    }
}

dependencies {

    // Core-UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.browser)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Retrofit
    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)

    // OkHttp
    implementation(platform(libs.okhttp.bom))
    implementation(libs.logging.interceptor)

    // Debug-implementation
    debugImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.test.rules)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Unit-tests
    testImplementation(libs.junit)
    testImplementation(libs.androidx.paging.testing.android)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    // UI-tests
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.coil.test.android)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.paging.testing.android)
}