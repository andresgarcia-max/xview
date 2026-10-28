plugins {
    id("com.android.application")
}
android {
    namespace = "com.family.launcher"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.family.launcher"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-B866F"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}
