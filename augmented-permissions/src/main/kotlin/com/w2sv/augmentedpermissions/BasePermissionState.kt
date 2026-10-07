package com.w2sv.augmentedpermissions

import kotlinx.coroutines.flow.SharedFlow

internal abstract class BasePermissionState(
    private val requestHistory: () -> PermissionRequestHistory?,
    private val wasRequestLaunchedBefore: () -> Boolean?,
    override val grantedFromRequest: SharedFlow<Boolean>,
    private val onRequestSuppressed: () -> Unit
) : PermissionState {

    final override val isLaunchingSuppressed: Boolean
        get() = !isGranted && wasRequestLaunchedBefore() == true && !shouldShowRationale

    final override fun launchRequest(onSuppressed: (() -> Unit)?) {
        if (isLaunchingSuppressed) {
            (onSuppressed ?: onRequestSuppressed).invoke()
        } else {
            launchPlatformRequest()
            if (wasRequestLaunchedBefore() == false) {
                requestHistory()?.recordRequestLaunched?.invoke()
            }
        }
    }

    protected abstract fun launchPlatformRequest()
}
