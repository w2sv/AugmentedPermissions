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

    testImplementation(libs.androidx.activity.compose)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
