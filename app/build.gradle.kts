import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("com.google.gms.google-services")
}

val properties = Properties()
val inputStream = project.rootProject.file("local.properties").inputStream()
properties.load( inputStream )

android {
    namespace = "com.hyphenate.chat.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hyphenate.chat.demo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField ("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField ("String", "APP_SERVER_DOMAIN", "\"a1.easemob.com\"")
        buildConfigField ("String", "APP_BASE_USER", "\"/inside/app/user/\"")
        buildConfigField ("String", "APP_SERVER_LOGIN", "\"login/V2\"")
        buildConfigField ("String", "APP_SERVER_REGISTER", "\"register\"")
        buildConfigField ("String", "APP_SERVE_CHECK_RESET", "\"reset/password\"")
        buildConfigField ("String", "APP_SERVE_CHANGE_PWD", "\"/password\"")
        buildConfigField ("String", "APP_SEND_SMS_FROM_SERVER", "\"/inside/app/sms/send\"")
        buildConfigField ("String", "APP_VERIFICATION_CODE", "\"/inside/app/image/\"")

        buildConfigField("String", "APPKEY", "\"${properties.getProperty("APPKEY")}\"")

        // Set push info from local.properties
        buildConfigField("String", "MEIZU_PUSH_APPKEY", "\"${properties.getProperty("MEIZU_PUSH_APPKEY")}\"")
        buildConfigField("String", "MEIZU_PUSH_APPID", "\"${properties.getProperty("MEIZU_PUSH_APPID")}\"")
        buildConfigField("String", "OPPO_PUSH_APPKEY", "\"${properties.getProperty("OPPO_PUSH_APPKEY")}\"")
        buildConfigField("String", "OPPO_PUSH_APPSECRET", "\"${properties.getProperty("OPPO_PUSH_APPSECRET")}\"")
        buildConfigField("String", "VIVO_PUSH_APPID", "\"${properties.getProperty("VIVO_PUSH_APPID")}\"")
        buildConfigField("String", "VIVO_PUSH_APPKEY", "\"${properties.getProperty("VIVO_PUSH_APPKEY")}\"")
        buildConfigField("String", "MI_PUSH_APPKEY", "\"${properties.getProperty("MI_PUSH_APPKEY")}\"")
        buildConfigField("String", "MI_PUSH_APPID", "\"${properties.getProperty("MI_PUSH_APPID")}\"")
        buildConfigField("String", "FCM_SENDERID", "\"${properties.getProperty("FCM_SENDERID")}\"")
        buildConfigField("String", "HONOR_PUSH_APPID", "\"${properties.getProperty("HONOR_PUSH_APPID")}\"")

        addManifestPlaceholders(mapOf(
            "VIVO_PUSH_APPKEY" to properties.getProperty("VIVO_PUSH_APPKEY", "******"),
            "VIVO_PUSH_APPID" to properties.getProperty("VIVO_PUSH_APPID", "******"),
            "HONOR_PUSH_APPID" to properties.getProperty("HONOR_PUSH_APPID", "******")
        ))
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
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    applicationVariants.all {
        outputs.all { it ->
            val apkName = "easemob_demo_${buildType.name}_${versionName}.apk"
            if (it is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                it.outputFileName = apkName
            }
            true
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("pub.devrel:easypermissions:3.0.0")
    // hms push
    implementation("com.huawei.hms:push:6.3.0.302")
    // hihonor push
    implementation("com.hihonor.mcs:push:7.0.41.301")
    // meizu push
    implementation("com.meizu.flyme.internet:push-internal:4.0.4@aar")//配置集成sdk
    //oppo push
    implementation(files("libs/oppo_push_3.0.0.aar"))
    //oppo push需添加以下依赖
    implementation("com.google.code.gson:gson:2.6.2")
    implementation("commons-codec:commons-codec:1.6")
    implementation("androidx.annotation:annotation:1.1.0")
    // Google firebase cloud messaging
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:29.1.0"))

    // Declare the dependencies for the Firebase Cloud Messaging and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    implementation(project(mapOf("path" to ":ease-im-kit")))
}