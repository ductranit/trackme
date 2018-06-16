/**
 * This file contains all versions of all dependencies and app configurations
 */

object Configs {
    val appId = "ductranit.me.trackme"
    val versionCode = 1
    val versionName = "1.0"

    val buildTools = "27.0.3"
    val compileSdk = 27
    val minSdk = 21
    val targetSdk = 27
    val gradleBuildTool = "3.1.2"
    val kotlin = "1.2.50"
}

object Version {
    val support = "27.1.1"
    val constraint = "1.1.1"
    val architecture = "1.1.0"
    val archPagging = "1.0.0-beta1"
    val dagger2 = "2.15"
    val playService = "15.0.1"

    var gson = "2.8.2"
    val glide = "4.7.1"
    val rxAndroid = "2.0.2"
    val rxJava = "2.1.14"
    val leakCanary = "1.5.4"
    val objectBox = "1.5.0"
    val objectBoxRxJava = "0.9.8"
    val timber = "4.7.0"

    val junit = "4.12"
    val testRunner = "1.0.1"
    val espresso = "3.0.1"
}

object Deps {
    val supportLibs = arrayOf("com.android.support:appcompat-v7:${Version.support}"
            , "com.android.support:design:${Version.support}"
            , "com.android.support:cardview-v7:${Version.support}"
            , "com.android.support:recyclerview-v7:${Version.support}"
            , "com.android.support.constraint:constraint-layout:${Version.constraint}")

    val architecture = arrayOf(
            "android.arch.lifecycle:extensions:${Version.architecture}"
            , "android.arch.paging:runtime:${Version.archPagging}"
    )

    val dagger2 = arrayOf(
            "com.google.dagger:dagger:${Version.dagger2}"
            , "com.google.dagger:dagger-android:${Version.dagger2}"
            , "com.google.dagger:dagger-android-support:${Version.dagger2}"
    )

    val dagger2Annotation = arrayOf(
            "com.google.dagger:dagger-android-processor:${Version.dagger2}"
            , "com.google.dagger:dagger-compiler:${Version.dagger2}"
    )

    val playServices = arrayOf(
            "com.google.android.gms:play-services-maps:${Version.playService}"
            ,"com.google.android.gms:play-services-location:${Version.playService}"
    )
}