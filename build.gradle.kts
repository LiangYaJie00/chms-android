// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        // 华为仓库 - 暂时注释掉
        // maven { url = uri("https://developer.huawei.com/repo/") }
        // FCM仓库
        maven { url = uri("https://maven.google.com") }
    }
    
    dependencies {
        // 添加Android Gradle插件依赖
        classpath("com.android.tools.build:gradle:8.7.2")
        
        // FCM配置 - 暂时注释掉
        // classpath("com.google.gms:google-services:4.3.15")
        // 华为配置 - 暂时注释掉
        // classpath("com.huawei.agconnect:agcp:1.6.0.300")
    }
}

// 移除这个块，因为它在新版Gradle中已被弃用
// allprojects {
//     repositories {
//         google()
//         mavenCentral()
//         maven { url = uri("https://www.jitpack.io") }
//         // 华为仓库
//         maven { url = uri("https://developer.huawei.com/repo/") }
//         // FCM仓库
//         maven { url = uri("https://maven.google.com") }
//     }
// }