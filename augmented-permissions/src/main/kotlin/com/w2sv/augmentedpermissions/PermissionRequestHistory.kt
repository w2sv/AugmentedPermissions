package com.w2sv.augmentedpermissions

import kotlinx.coroutines.flow.Flow

/**
 * Bundles an application's persisted request history with the action that records a launch.
 *
 * The library collects [wasRequestLaunchedBefore] while the permission state is composed.
 * The flow can initially emit `false` while persistence loads; no separate loading state
 * is required. [recordRequestLaunched] should arrange for a later `true` emission.
 */
class PermissionRequestHistory(val wasRequestLaunchedBefore: Flow<Boolean>, val recordRequestLaunched: () -> Unit)
