/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.theme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.TypedValue
import android.view.Window
import androidx.annotation.StyleRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.content.ContextCompat
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.view.getWindowInsetsController
import org.mozilla.fenix.Config
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.browsingmode.BrowsingMode
import org.mozilla.fenix.components.toolbar.ToolbarPosition
import org.mozilla.fenix.customtabs.ExternalAppBrowserActivity
import org.mozilla.fenix.ext.settings

abstract class ThemeManager(
    private val context: Context
) {

    abstract var currentTheme: BrowsingMode

    /**
     * Returns the style resource corresponding to the [currentTheme].
     */
    @get:StyleRes
    val currentThemeResource get() = when (currentTheme) {
        BrowsingMode.Normal -> R.style.NormalTheme
        BrowsingMode.Private -> {
            if (Config.channel.isMax) {
                when (context.settings().toolbarPosition) {
                    ToolbarPosition.TOP -> R.style.PrivateTopTheme
                    ToolbarPosition.BOTTOM -> R.style.PrivateBottomTheme
                }
            } else {
                R.style.PrivateTheme
            }
        }
    }

    /**
     * Handles status bar theme change since the window does not dynamically recreate
     */
    fun applyStatusBarTheme(activity: Activity) = applyStatusBarTheme(activity.window, activity)
    fun applyStatusBarTheme(window: Window, context: Context) {
        when (currentTheme) {
            BrowsingMode.Normal -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_UNDEFINED, // We assume light here per Android doc's recommendation
                    Configuration.UI_MODE_NIGHT_NO,
                    -> {
                        updateLightSystemBars(window, context)
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        clearLightSystemBars(window, context)
                        updateNavigationBar(window, context)
                    }
                }
            }
            BrowsingMode.Private -> {
                clearLightSystemBars(window, context)
                updateNavigationBar(window, context)
            }
        }
    }

    fun setActivityTheme(activity: Activity) {
        activity.setTheme(currentThemeResource)
    }

    companion object {
        fun resolveAttribute(attribute: Int, context: Context): Int {
            val typedValue = TypedValue()
            val theme = context.theme
            theme.resolveAttribute(attribute, typedValue, true)

            return typedValue.resourceId
        }

        @Composable
        fun resolveAttributeColor(attribute: Int): androidx.compose.ui.graphics.Color {
            val resourceId = resolveAttribute(attribute, LocalContext.current)
            return colorResource(resourceId)
        }

        private fun updateLightSystemBars(window: Window, context: Context) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = context.getColorFromAttr(android.R.attr.statusBarColor)
                window.getWindowInsetsController().isAppearanceLightStatusBars = true
            } else {
                window.statusBarColor = Color.BLACK
            }

            if (SDK_INT >= Build.VERSION_CODES.O) {
                // API level can display handle light navigation bar color
                window.getWindowInsetsController().isAppearanceLightNavigationBars = true

                updateLightNavigationBar(window, context)
            }
        }

        private fun clearLightSystemBars(window: Window, context: Context) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = context.getColor(android.R.color.transparent)
                window.getWindowInsetsController().isAppearanceLightStatusBars = false
            }

            if (SDK_INT >= Build.VERSION_CODES.O) {
                // API level can display handle light navigation bar color
                window.getWindowInsetsController().isAppearanceLightNavigationBars = false
            }
        }

        private fun updateNavigationBar(window: Window, context: Context) {
            window.navigationBarColor = context.getColorFromAttr(R.attr.layer1)
        }

        private fun updateLightNavigationBar(window: Window, context: Context) {
            if (Config.channel.isMax) {
                window.navigationBarColor = ContextCompat.getColor(context, R.color.day_light_layer_1_color)
            } else {
                window.navigationBarColor = context.getColorFromAttr(R.attr.layer1)
            }
        }
    }
}

class DefaultThemeManager(
    currentTheme: BrowsingMode,
    private val activity: Activity,
) : ThemeManager(activity) {
    override var currentTheme: BrowsingMode = currentTheme
        set(value) {
            if (currentTheme != value) {
                // ExternalAppBrowserActivity doesn't need to switch between private and non-private.
                if (activity is ExternalAppBrowserActivity) return
                // Don't recreate if activity is finishing
                if (activity.isFinishing) return

                field = value

                val intent = activity.intent ?: Intent().also { activity.intent = it }
                intent.putExtra(HomeActivity.PRIVATE_BROWSING_MODE, value == BrowsingMode.Private)

                activity.recreate()
            }
        }
}
