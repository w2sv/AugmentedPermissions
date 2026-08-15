package com.w2sv.augmentedpermissions

import com.google.accompanist.permissions.PermissionState as AccompanistPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.flow.SharedFlow

internal class SinglePermissionState(
    private val accompanistPermissionState: AccompanistPermissionState,
    requestHistory: () -> PermissionRequestHistory,
    grantedFromRequest: SharedFlow<Boolean>,
    onRequestSuppressed: () -> Unit
) : BasePermissionState(
    requestHistory = requestHistory,
    grantedFromRequest = grantedFromRequest,
    onRequestSuppressed = onRequestSuppressed
) {
    override val isGranted: Boolean
        get() = accompanistPermissionState.status.isGranted

    override val shouldShowRationale: Boolean
        get() = accompanistPermissionState.status.shouldShowRationale

    override val revokedPermissions: List<String>
        get() = if (isGranted) {
            emptyList()
        } else {
            listOf(accompanistPermissionState.permission)
        }

    override fun launchPlatformRequest() {
        accompanistPermissionState.launchPermissionRequest()
    }
}
