<h1 align="center">AugmentedPermissions</h1>

<p align="center">
    <a href="https://android-arsenal.com/api?level=21">
        <img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" alt="API">
    </a>
    <img src="https://img.shields.io/maven-central/v/io.github.w2sv/augmented-permissions" alt="Maven Central Version">
    <a href="https://github.com/w2sv/AugmentedPermissions/actions/workflows/build.yaml">
        <img src="https://github.com/w2sv/AugmentedPermissions/actions/workflows/build.yaml/badge.svg" alt="Build">
    </a>
    <img src="https://img.shields.io/github/license/w2sv/AugmentedPermissions" alt="GitHub License">
</p>

Augmented Permissions is a higher-level permission API for Jetpack Compose, built on
[Accompanist Permissions](https://google.github.io/accompanist/permissions/).

Its primary motivation is closing a gap in both Android's stock permission API and Accompanist Permissions: neither provides an unambiguous way to react when launching a permission request is suppressed and no system dialog appears.
Augmented Permissions addresses this by combining Android's rationale state with persisted request history. This allows a `PermissionState` to distinguish an initial request from one that has already been made and is now considered suppressed.
Applications remain responsible for persisting the request history for each permission requirement through `PermissionRequestHistory`, keeping the library independent of any particular storage solution.

```kotlin
val permissionState = rememberPermissionState(
    permission = Manifest.permission.CAMERA,
    requestHistory = cameraPermissionHistory,
    onRequestSuppressed = { openAppSettings() }
)

permissionState.launchRequest()
```

Beyond suppression handling, Augmented Permissions provides a few additional conveniences:

* a unified `PermissionState` for single and multiple permissions
* a consistent `isGranted`, `shouldShowRationale`, and `revokedPermissions` API
* `isLaunchingSuppressed` for inspecting suppression state directly
* `grantedFromRequest` as a `SharedFlow`, allowing multiple consumers to independently react to permission-request results
* no Accompanist types in the public API

## Installation

### Inline

```kotlin
dependencies {
    implementation("io.github.w2sv:augmented-permissions:<version>")
}
```

### Version Catalog (`libs.versions.toml`)

```toml
[versions]
w2sv-augmented-permissions = "<version>"

[libraries]
w2sv-augmented-permissions = { module = "io.github.w2sv:augmented-permissions", version.ref = "w2sv-augmented-permissions" }
```

Then add the alias:

```kotlin
dependencies {
    implementation(libs.w2sv.augmented.permissions)
}
```

## License

Licensed under the [Apache License 2.0](LICENSE).
