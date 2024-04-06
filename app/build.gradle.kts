plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Python
    id("com.chaquo.python")
    id("com.google.devtools.ksp")
    // Hilt DI
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    // Firebase
    id("com.google.gms.google-services")
    // Parcelize
    id("kotlin-parcelize")
}

android {
    namespace = "com.martinszuc.phishing_emails_detection"

    compileSdk = 34

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    defaultConfig {
        applicationId = "com.martinszuc.phishing_emails_detection"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Chaquopy
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

chaquopy {
    defaultConfig {
        version = "3.8"
        buildPython("/home/mszuc/.pyenv/versions/jupyterlab_env/bin/python3.8")
        pip {
            install ("protobuf==3.20.0")
            install ("tensorflow==2.1.0")               // TODO retraining retrained model fixed in tensorflow==2.2 https://github.com/tensorflow/tensorflow/issues/39221
            install ("numpy")
            install ("scipy")
            install ("scikit-learn")
            install ("joblib")
            install ("pandas")
            install ("beautifulsoup4==4.10.0")
        }
    }
    productFlavors { }
    sourceSets { }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.gms:play-services-base:18.3.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.46")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    kapt("com.google.dagger:hilt-android-compiler:2.46")

    // Paging and DB
    implementation("androidx.room:room-paging:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Swipe views
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Settings fragment preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Retrofit network communication
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt {
    correctErrorTypes = true
}