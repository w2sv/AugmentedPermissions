package com.w2sv.augmentedpermissions

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharedFlow

/**
 * A unified permission state augmenting Accompanist's single- and
 * multiple-permission state APIs with request tracking and result observation.
 *
 * Compared to Accompanist's permission states, this abstraction:
 * - provides the same [isGranted], [shouldShowRationale], and [revokedPermissions]
 *   surface regardless of whether one or multiple permissions are represented
 * - exposes [grantedFromRequest], allowing multiple consumers to independently
 *   observe the result of explicit permission requests
 * - tracks whether a request has previously been launched so that a request that
 *   is considered suppressed can be handled explicitly through [launchRequest]
 *
 * The distinction between a single permission and a set of permissions is an
 * implementation detail. [isGranted] represents whether the complete permission
 * requirement is satisfied, while [revokedPermissions] allows consumers to inspect
 * individual permissions when necessary, for example to provide permission-specific
 * rationale UI.
 */
@Stable
interface PermissionState {

    /**
     * Whether the complete represented permission requirement is currently granted.
     *
     * For a single permission this corresponds to `PermissionStatus.isGranted`.
     * For multiple permissions this corresponds to
     * `MultiplePermissionsState.allPermissionsGranted`.
     */
    val isGranted: Boolean

    /**
     * Whether Android indicates that a rationale should currently be shown for
     * the represented permission requirement.
     *
     * For multiple permissions this follows Accompanist's aggregate
     * `MultiplePermissionsState.shouldShowRationale` semantics.
     */
    val shouldShowRationale: Boolean

    /**
     * Whether launching the represented permission request is considered suppressed.
     *
     * A request is considered suppressed when it has previously been launched and
     * Android no longer indicates that a permission rationale should be shown.
     */
    val isLaunchingSuppressed: Boolean

    /**
     * The represented permissions that are currently not granted.
     *
     * For a single permission this is either empty or contains that permission.
     * For multiple permissions this corresponds to
     * `MultiplePermissionsState.revokedPermissions`.
     */
    val revokedPermissions: List<String>

    /**
     * Results produced by explicitly launched permission requests.
     *
     * An emission of `true` means that the complete represented permission
     * requirement was granted by the request result; `false` means that at least
     * one represented permission was not granted.
     *
     * This is an event stream and does not represent the current permission state.
     * Use [isGranted] for that.
     */
    val grantedFromRequest: SharedFlow<Boolean>

    /**
     * Launches the represented permission request unless it is considered suppressed.
     *
     * A request is considered suppressed when it has previously been launched and
     * Android no longer indicates that a rationale should be shown. In that case,
     * [onSuppressed] is invoked instead of launching the platform request.
     *
     * When [onSuppressed] is `null`, the default suppression callback supplied to
     * [rememberPermissionState] is used.
     */
    fun launchRequest(onSuppressed: (() -> Unit)? = null)
}
