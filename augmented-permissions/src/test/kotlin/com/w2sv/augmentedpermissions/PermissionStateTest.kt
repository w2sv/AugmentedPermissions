package com.w2sv.augmentedpermissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PermissionStateTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val cameraPermission = Manifest.permission.CAMERA
    private val audioPermission = Manifest.permission.RECORD_AUDIO
    private val permissions = listOf(cameraPermission, audioPermission)

    @Test
    fun `single permission exposes granted state`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(cameraPermission)

        val state = setPermissionContent {
            rememberPermissionState(cameraPermission, TestPermissionRequestHistory().history)
        }

        assertState(state, isGranted = true)
    }

    @Test
    fun `single permission exposes revoked state`() {
        val state = setPermissionContent {
            rememberPermissionState(cameraPermission, TestPermissionRequestHistory().history)
        }

        assertState(state, revokedPermissions = listOf(cameraPermission))
    }

    @Test
    fun `multiple permissions expose aggregate and individual state`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(cameraPermission)

        val state = setPermissionContent {
            rememberPermissionState(permissions, TestPermissionRequestHistory().history)
        }

        assertState(state, revokedPermissions = listOf(audioPermission))
    }

    @Test
    fun `request is suppressed only after a previous launch without rationale`() {
        val state = setPermissionContent {
            rememberPermissionState(cameraPermission, TestPermissionRequestHistory(true).history)
        }

        assertState(
            state = state,
            isLaunchingSuppressed = true,
            revokedPermissions = listOf(cameraPermission)
        )
    }

    @Test
    fun `granted permission is not considered suppressed despite request history`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(cameraPermission)

        val state = setPermissionContent {
            rememberPermissionState(cameraPermission, TestPermissionRequestHistory(true).history)
        }

        assertState(state, isGranted = true)
    }

    @Test
    fun `untracked single permission launches and reports its result`() {
        var result: Boolean? = null
        val state = setPermissionContent {
            rememberPermissionState(
                permission = cameraPermission,
                requestHistory = null,
                onPermissionResult = { result = it }
            )
        }

        assertState(state, revokedPermissions = listOf(cameraPermission))
        launchAndRespond(state, cameraPermission to false)
        composeTestRule.waitUntil { result != null }

        assertEquals(false, result)
    }

    @Test
    fun `untracked multiple permissions launch and report their results`() {
        var result: Map<String, Boolean>? = null
        val state = setPermissionContent {
            rememberPermissionState(
                permissions = permissions,
                requestHistory = null,
                onPermissionsResult = { result = it }
            )
        }

        assertState(state, revokedPermissions = permissions)
        launchAndRespond(state, cameraPermission to true, audioPermission to false)
        composeTestRule.waitUntil { result != null }

        assertEquals(mapOf(cameraPermission to true, audioPermission to false), result)
    }

    @Test
    fun `rationale keeps a repeated request launchable`() {
        shadowOf(composeTestRule.activity.packageManager)
            .setShouldShowRequestPermissionRationale(cameraPermission, true)

        val state = setPermissionContent {
            rememberPermissionState(cameraPermission, TestPermissionRequestHistory(true).history)
        }

        assertState(
            state = state,
            shouldShowRationale = true,
            revokedPermissions = listOf(cameraPermission)
        )
    }

    @Test
    fun `suppressed request uses default or per-launch callback`() {
        val callbacks = mutableListOf<String>()
        val state = setPermissionContent {
            rememberPermissionState(
                permission = cameraPermission,
                requestHistory = TestPermissionRequestHistory(true).history,
                onRequestSuppressed = { callbacks += "default" }
            )
        }

        composeTestRule.runOnIdle {
            state.launchRequest()
            state.launchRequest { callbacks += "override" }
        }

        assertEquals(listOf("default", "override"), callbacks)
    }

    @Test
    fun `single request records history and publishes its granted result`() {
        val history = TestPermissionRequestHistory()
        var callbackResult: Boolean? = null
        var flowResult: Boolean? = null
        val state = setPermissionContent(onFlowResult = { flowResult = it }) {
            rememberPermissionState(
                permission = cameraPermission,
                requestHistory = history.history,
                onPermissionResult = { callbackResult = it }
            )
        }

        launchAndRespond(state, cameraPermission to true)
        composeTestRule.waitUntil { callbackResult != null && flowResult != null }

        assertEquals(true, callbackResult)
        assertEquals(true, flowResult)
        assertRecordedOnce(history)
    }

    @Test
    fun `request history is recorded when the request launches`() {
        val history = TestPermissionRequestHistory()
        val state = setPermissionContent {
            rememberPermissionState(
                cameraPermission,
                requestHistory = history.history
            )
        }

        composeTestRule.runOnIdle {
            state.launchRequest()
            assertRecordedOnce(history)
        }
    }

    @Test
    fun `history flow updates suppression state`() {
        val history = TestPermissionRequestHistory()
        val state = setPermissionContent {
            rememberPermissionState(
                permission = cameraPermission,
                requestHistory = history.history
            )
        }

        assertState(state, revokedPermissions = listOf(cameraPermission))
        composeTestRule.runOnIdle {
            history.wasRequestLaunchedBefore.value = true
        }
        composeTestRule.waitUntil { state.isLaunchingSuppressed }

        assertState(state, isLaunchingSuppressed = true, revokedPermissions = listOf(cameraPermission))
    }

    @Test
    fun `multiple request publishes individual and aggregate results`() {
        val history = TestPermissionRequestHistory()
        var callbackResult: Map<String, Boolean>? = null
        var flowResult: Boolean? = null
        val state = setPermissionContent(onFlowResult = { flowResult = it }) {
            rememberPermissionState(
                permissions = permissions,
                requestHistory = history.history,
                onPermissionsResult = { callbackResult = it }
            )
        }

        launchAndRespond(
            state,
            cameraPermission to true,
            audioPermission to false
        )
        composeTestRule.waitUntil { callbackResult != null && flowResult != null }

        assertEquals(
            mapOf(cameraPermission to true, audioPermission to false),
            callbackResult
        )
        assertEquals(false, flowResult)
        assertRecordedOnce(history)
    }

    private fun setPermissionContent(
        onFlowResult: ((Boolean) -> Unit)? = null,
        stateProvider: @Composable () -> PermissionState
    ): PermissionState {
        lateinit var state: PermissionState

        composeTestRule.setContent {
            state = stateProvider()
            if (onFlowResult != null) {
                LaunchedEffect(state) {
                    state.grantedFromRequest.collect(onFlowResult)
                }
            }
        }
        composeTestRule.waitForIdle()

        return state
    }

    private fun assertState(
        state: PermissionState,
        isGranted: Boolean = false,
        shouldShowRationale: Boolean = false,
        isLaunchingSuppressed: Boolean = false,
        revokedPermissions: List<String> = emptyList()
    ) {
        composeTestRule.runOnIdle {
            assertEquals(isGranted, state.isGranted)
            assertEquals(shouldShowRationale, state.shouldShowRationale)
            assertEquals(isLaunchingSuppressed, state.isLaunchingSuppressed)
            assertEquals(revokedPermissions, state.revokedPermissions)
        }
    }

    private fun launchAndRespond(state: PermissionState, vararg results: Pair<String, Boolean>) {
        composeTestRule.runOnIdle { state.launchRequest() }

        val request = shadowOf(composeTestRule.activity).lastRequestedPermission
        val resultsByPermission = results.toMap()
        val grantResults = request.requestedPermissions
            .map { permission ->
                if (resultsByPermission.getValue(permission)) {
                    PackageManager.PERMISSION_GRANTED
                } else {
                    PackageManager.PERMISSION_DENIED
                }
            }
            .toIntArray()
        val resultData = Intent(ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS)
            .putExtra(
                ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS,
                request.requestedPermissions
            )
            .putExtra(
                ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS,
                grantResults
            )

        composeTestRule.runOnIdle {
            composeTestRule.activity.activityResultRegistry.dispatchResult(
                request.requestCode,
                Activity.RESULT_OK,
                resultData
            )
        }
    }

    private fun assertRecordedOnce(history: TestPermissionRequestHistory) {
        assertEquals(1, history.recordCount)
        assertEquals(true, history.wasRequestLaunchedBefore.value)
    }
}

private class TestPermissionRequestHistory(requestedBefore: Boolean = false) {
    val wasRequestLaunchedBefore = MutableStateFlow(requestedBefore)
    val history = PermissionRequestHistory(wasRequestLaunchedBefore, ::recordRequestLaunched)

    var recordCount = 0
        private set

    fun recordRequestLaunched() {
        recordCount++
        wasRequestLaunchedBefore.value = true
    }
}
