package com.w2sv.augmentedpermissions

/**
 * Provides the persisted request history required by [PermissionState]
 * to distinguish an initial permission request from one that is considered
 * suppressed.
 *
 * The history's storage mechanism is deliberately left to the consumer.
 * [recordRequestLaunched] should cause [wasRequestLaunchedBefore] to subsequently
 * reflect that a request has already been made.
 */
interface PermissionRequestHistory {

    /**
     * Whether the represented permission request has previously been launched.
     * This must NOT be a compose snapshot value.
     */
    val wasRequestLaunchedBefore: Boolean

    /**
     * Records that the represented permission request has been launched, thereby changing the subsequent return value of
     * [wasRequestLaunchedBefore].
     * Will only be called if [wasRequestLaunchedBefore] returns false on receiving the permission request result.
     */
    fun recordRequestLaunched()
}
