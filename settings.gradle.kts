pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 添加华为仓库
        maven { url = uri("https://developer.huawei.com/repo/") }
        // 添加FCM仓库
        maven { url = uri("https://maven.google.com") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        // 确保华为仓库已添加
        maven { url = uri("https://developer.huawei.com/repo/") }
        // 确保FCM仓库已添加
        maven { url = uri("https://maven.google.com") }
    }
}

rootProject.name = "CHMS-Android"
include(":app")
include(":tuicallkit-kt")
