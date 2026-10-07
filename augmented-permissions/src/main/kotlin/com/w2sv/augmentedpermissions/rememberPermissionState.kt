package com.w2sv.augmentedpermissions

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.google.accompanist.permissions.rememberMultiplePermissionsState as rememberAccompanistMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState as rememberAccompanistPermissionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Creates and remembers a [PermissionState] representing a single permission.
 *
 * The returned state augments Accompanist's `rememberPermissionState` with persisted
 * request tracking, suppression handling, a normalized permission-state surface,
 * and [PermissionState.grantedFromRequest] for observing explicit request
 * results from multiple consumers.
 *
 * [requestHistory] supplies persisted request history and records a launch while
 * its latest collected value is false. Pass `null` to launch without tracking or
 * suppression.
 *
 * [onPermissionResult] receives the result of each explicit permission request.
 * [onRequestSuppressed] is invoked when [PermissionState.launchRequest]
 * considers launching suppressed and no per-call suppression callback was provided.
 */
@Composable
fun rememberPermissionState(
    permission: String,
    requestHistory: PermissionRequestHistory?,
    onPermissionResult: (Boolean) -> Unit = {},
    onRequestSuppressed: () -> Unit = {}
): PermissionState {
    val scope = rememberCoroutineScope()

    val grantedFromRequest = remember(permission) { MutableSharedFlow<Boolean>() }

    val wasRequestLaunchedBefore = requestHistory?.wasRequestLaunchedBefore?.collectAsState(initial = false)?.value
    val currentRequestHistory = rememberUpdatedState(requestHistory)
    val currentWasRequestLaunchedBefore = rememberUpdatedState(wasRequestLaunchedBefore)
    val currentOnPermissionResult = rememberUpdatedState(onPermissionResult)
    val currentOnRequestSuppressed = rememberUpdatedState(onRequestSuppressed)

    val permissionState = rememberAccompanistPermissionState(
        permission = permission,
        onPermissionResult = { granted ->
            currentOnPermissionResult.value(granted)

            scope.launch { grantedFromRequest.emit(granted) }
        }
    )

    return remember(permissionState, grantedFromRequest) {
        SinglePermissionState(
            accompanistPermissionState = permissionState,
            requestHistory = { currentRequestHistory.value },
            wasRequestLaunchedBefore = { currentWasRequestLaunchedBefore.value },
            grantedFromRequest = grantedFromRequest,
            onRequestSuppressed = { currentOnRequestSuppressed.value() }
        )
    }
}

/**
 * Creates and remembers a [PermissionState] representing multiple permissions.
 *
 * The returned state augments Accompanist's `rememberMultiplePermissionsState` with
 * persisted request tracking, suppression handling, a normalized permission-state
 * surface, and [PermissionState.grantedFromRequest] for observing whether
 * explicit requests granted the complete represented permission requirement.
 *
 * [requestHistory] supplies persisted request history and records a launch while
 * its latest collected value is false. Pass `null` to launch without tracking or
 * suppression.
 *
 * [onPermissionsResult] receives Accompanist's individual result for every requested
 * permission. [onRequestSuppressed] is invoked when
 * [PermissionState.launchRequest] considers launching suppressed and no
 * per-call suppression callback was provided.
 */
@SuppressLint("ComposeUnstableCollections")
@Composable
fun rememberPermissionState(
    permissions: List<String>,
    requestHistory: PermissionRequestHistory?,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    onRequestSuppressed: () -> Unit = {}
): PermissionState {
    val scope = rememberCoroutineScope()

    val grantedFromRequest = remember(permissions) { MutableSharedFlow<Boolean>() }

    val wasRequestLaunchedBefore = requestHistory?.wasRequestLaunchedBefore?.collectAsState(initial = false)?.value
    val currentRequestHistory = rememberUpdatedState(requestHistory)
    val currentWasRequestLaunchedBefore = rememberUpdatedState(wasRequestLaunchedBefore)
    val currentOnPermissionsResult = rememberUpdatedState(onPermissionsResult)
    val currentOnRequestSuppressed = rememberUpdatedState(onRequestSuppressed)

    val permissionsState = rememberAccompanistMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { result ->
            currentOnPermissionsResult.value(result)

            scope.launch {
                grantedFromRequest.emit(
                    result.values.all { granted -> granted }
                )
            }
        }
    )

    return remember(permissionsState, grantedFromRequest) {
        MultiplePermissionState(
            accompanistPermissionsState = permissionsState,
            requestHistory = { currentRequestHistory.value },
            wasRequestLaunchedBefore = { currentWasRequestLaunchedBefore.value },
            grantedFromRequest = grantedFromRequest,
            onRequestSuppressed = { currentOnRequestSuppressed.value() }
        )
    }
}
