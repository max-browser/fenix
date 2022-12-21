package org.mozilla.fenix.components.fullscreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.mediasession.MediaSession
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged

open class MaxFullscreenFeature (
    private val activity: Activity,
    private val store: BrowserStore,
    private val sessionUseCases: SessionUseCases,
    private val tabId: String? = null,
    private val viewportFitChanged: (Int) -> Unit = {},
    private val fullScreenChanged: (Boolean) -> Unit,
) : LifecycleAwareFeature, UserInteractionHandler {
    private var scope: CoroutineScope? = null
    private var observation: Observation = createDefaultObservation()

    /**
     * Starts the feature and a observer to listen for fullscreen changes.
     */
    override fun start() {
        scope = store.flowScoped { flow ->
            flow.map { state ->
                Pair(state.findTabOrCustomTabOrSelectedTab(tabId), state.tabs + state.customTabs)
            }.map { sessionPair ->
                Pair(
                    sessionPair.first.toObservation(),
                    sessionPair.second.filter { sessionState ->
                        sessionState.mediaSessionState != null && sessionState.mediaSessionState!!.fullscreen
                    }
                )
            }.ifChanged().collect { sessionResult ->
                processFullscreen(sessionResult)
                processDeviceSleepMode(sessionResult.second)
            }
        }
    }

    override fun stop() {
        scope?.cancel()
    }

    private fun onChange(observation: Observation) {
        if (observation.inFullScreen != this.observation.inFullScreen) {
            fullScreenChanged(observation.inFullScreen)
        }

        if (observation.layoutInDisplayCutoutMode != this.observation.layoutInDisplayCutoutMode) {
            viewportFitChanged(observation.layoutInDisplayCutoutMode)
        }

        this.observation = observation
    }

    /**
     * To be called when the back button is pressed, so that only fullscreen mode closes.
     *
     * @return Returns true if the fullscreen mode was successfully exited; false if no effect was taken.
     */
    override fun onBackPressed(): Boolean {
        val observation = observation

        if (observation.inFullScreen && observation.tabId != null) {
            sessionUseCases.exitFullscreen(observation.tabId)
            return true
        }

        return false
    }

    @Suppress("SourceLockedOrientationActivity") // We deliberately want to lock the orientation here.
    private fun processFullscreen(sessionResult: Pair<Observation, List<SessionState>>) {
        /* there should only be one fullscreen session */
        val sessionStates = sessionResult.second
        val activeState = sessionStates.firstOrNull()

        if (activeState == null || activeState.mediaSessionState?.fullscreen != true) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            return
        }

        if (store.state.findCustomTabOrSelectedTab(tabId)?.id == activeState.id) {
            when (activeState.mediaSessionState?.elementMetadata?.portrait) {
                true ->
                    activity.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
                false ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInPictureInPictureMode) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    } else {
                        activity.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                else -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            }
        }

        if (sessionStates.isNotEmpty() || !isMediaSessionExist(store)) {
            onChange(sessionResult.first)
        }
    }

    private fun processDeviceSleepMode(sessionStates: List<SessionState>) {
        val activeTabState = sessionStates.firstOrNull()
        if (activeTabState == null || activeTabState.mediaSessionState?.fullscreen != true) {
            return
        }
        activeTabState.mediaSessionState?.let {
            when (activeTabState.mediaSessionState?.playbackState) {
                MediaSession.PlaybackState.PLAYING -> {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                else -> {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }
}

/**
 * Simple holder data class to keep a reference to the last values we observed.
 */
private data class Observation(
    val tabId: String?,
    val inFullScreen: Boolean,
    val layoutInDisplayCutoutMode: Int,
)

private fun SessionState?.toObservation(): Observation {
    return if (this != null) {
        Observation(id, content.fullScreen, content.layoutInDisplayCutoutMode)
    } else {
        createDefaultObservation()
    }
}

private fun createDefaultObservation() = Observation(
    tabId = null,
    inFullScreen = false,
    layoutInDisplayCutoutMode = 0,
)

private fun isMediaSessionExist(store: BrowserStore): Boolean {
    var result = false
    (store.state.tabs + store.state.customTabs).forEach {
        if (it.mediaSessionState != null){
            result = true
            return@forEach
        }
    }
    return result
}
