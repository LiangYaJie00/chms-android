plugins {
    // 确保华为插件在其他插件之前应用
    // id("com.huawei.agconnect")  // 暂时注释掉华为插件
    
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    // FCM插件
    // id("com.google.gms.google-services")  // 暂时注释掉FCM插件
}

android {
    namespace = "com.example.chms_android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chms_android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 确保添加ndk配置
        ndk {
            abiFilters.add("armeabi")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            // 可选: abiFilters.add("x86"), abiFilters.add("x86_64")
        }
        
        // 更新厂商通道配置
        manifestPlaceholders["JPUSH_PKGNAME"] = "com.example.chms_android"
        manifestPlaceholders["JPUSH_APPKEY"] = "5cfe2141f6fd5c9ab46a0928" // 您的AppKey
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"

        // 添加厂商通道配置
        manifestPlaceholders["XIAOMI_APPID"] = "填写小米的APPID" // 需要申请
        manifestPlaceholders["XIAOMI_APPKEY"] = "填写小米的APPKEY" // 需要申请
        manifestPlaceholders["OPPO_APPKEY"] = "填写OPPO的APPKEY" // 需要申请
        manifestPlaceholders["OPPO_APPID"] = "填写OPPO的APPID" // 需要申请
        manifestPlaceholders["OPPO_APPSECRET"] = "填写OPPO的APPSECRET" // 需要申请
        manifestPlaceholders["VIVO_APPKEY"] = "填写VIVO的APPKEY" // 需要申请
        manifestPlaceholders["VIVO_APPID"] = "填写VIVO的APPID" // 需要申请
        manifestPlaceholders["MEIZU_APPKEY"] = "填写魅族的APPKEY" // 需要申请
        manifestPlaceholders["MEIZU_APPID"] = "填写魅族的APPID" // 需要申请
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")
    // https://mvnrepository.com/artifact/androidx.room/room-runtime
    implementation("androidx.room:room-runtime:2.5.2")
    // https://mvnrepository.com/artifact/androidx.room/room-compiler
    kapt("androidx.room:room-compiler:2.5.2")
    // https://mvnrepository.com/artifact/androidx.room/room-ktx
    implementation("androidx.room:room-ktx:2.5.2")
    // implementation("androidx.navigation:navigation-fragment:2.2.2")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.5.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.5.0")

    implementation("com.nambimobile.widgets:expandable-fab:1.0.2")
    // https://mvnrepository.com/artifact/com.github.bumptech.glide/glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("de.hdodenhof:circleimageview:2.1.0")

    api(project(":tuicallkit-kt"))

    implementation("cn.jiguang.sdk:jpush:5.6.0") // 必选，此处以JPush 5.6.0 版本为例

    // 华为HMS Core Push Kit - 暂时注释掉
    // implementation("com.huawei.hms:push:6.12.0.300")
    // implementation("cn.jiguang.sdk.plugin:huawei:5.5.3")

    // FCM通道 - 暂时注释掉
    // implementation("com.google.firebase:firebase-messaging:23.2.0")
    // implementation("cn.jiguang.sdk.plugin:fcm:5.5.3")

    // 保留其他厂商通道
    implementation("cn.jiguang.sdk.plugin:xiaomi:5.5.3")
    implementation("cn.jiguang.sdk.plugin:oppo:5.5.3")
    implementation("cn.jiguang.sdk.plugin:vivo:5.5.3.a")
    implementation("cn.jiguang.sdk.plugin:meizu:5.5.3")
}
