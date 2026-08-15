plugins {
    id("w2sv.android-compose-library")
}

kotlin {
    compilerOptions {
        optIn.add("com.google.accompanist.permissions.ExperimentalPermissionsApi")
    }
}

dependencies {
    implementation(libs.google.accompanist.permissions)
    api(libs.androidx.compose.runtime)
    api(libs.kotlinx.coroutines.core)
}
