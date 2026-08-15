package com.w2sv.augmentedpermissions

import kotlinx.coroutines.flow.SharedFlow

internal abstract class BasePermissionState(
    private val requestHistory: () -> PermissionRequestHistory,
    override val grantedFromRequest: SharedFlow<Boolean>,
    private val onRequestSuppressed: () -> Unit
) : PermissionState {

    final override val isLaunchingSuppressed: Boolean
        get() = requestHistory().wasRequestLaunchedBefore && !shouldShowRationale

    final override fun launchRequest(onSuppressed: (() -> Unit)?) {
        if (isLaunchingSuppressed) {
            (onSuppressed ?: onRequestSuppressed).invoke()
        } else {
            launchPlatformRequest()
        }
    }

    protected abstract fun launchPlatformRequest()
}
