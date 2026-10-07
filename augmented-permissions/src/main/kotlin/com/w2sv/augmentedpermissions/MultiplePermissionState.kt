package com.w2sv.augmentedpermissions

import com.google.accompanist.permissions.MultiplePermissionsState as AccompanistMultiplePermissionsState
import kotlinx.coroutines.flow.SharedFlow

internal class MultiplePermissionState(
    private val accompanistPermissionsState: AccompanistMultiplePermissionsState,
    requestHistory: () -> PermissionRequestHistory?,
    wasRequestLaunchedBefore: () -> Boolean?,
    grantedFromRequest: SharedFlow<Boolean>,
    onRequestSuppressed: () -> Unit
) : BasePermissionState(
    requestHistory = requestHistory,
    wasRequestLaunchedBefore = wasRequestLaunchedBefore,
    grantedFromRequest = grantedFromRequest,
    onRequestSuppressed = onRequestSuppressed
) {
    override val isGranted: Boolean
        get() = accompanistPermissionsState.allPermissionsGranted

    override val shouldShowRationale: Boolean
        get() = accompanistPermissionsState.shouldShowRationale

    override val revokedPermissions: List<String>
        get() = accompanistPermissionsState.revokedPermissions.map { it.permission }

    override fun launchPlatformRequest() {
        accompanistPermissionsState.launchMultiplePermissionRequest()
    }
}
