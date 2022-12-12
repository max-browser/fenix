/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.max.browser.core.ReportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuHighlight
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.ext.getHighlight
import mozilla.components.browser.menu.item.*
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.support.ktx.android.content.getColorFromAttr
import org.mozilla.fenix.Config
import org.mozilla.fenix.R
import org.mozilla.fenix.components.accounts.AccountState
import org.mozilla.fenix.components.accounts.FenixAccountManager
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.nimbus.FxNimbus
import org.mozilla.fenix.theme.ThemeManager

@Suppress("LargeClass", "LongMethod")
class HomeMenu(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val onItemTapped: (Item) -> Unit = {},
    private val onMenuBuilderChanged: (BrowserMenuBuilder) -> Unit = {},
    private val onHighlightPresent: (BrowserMenuHighlight) -> Unit = {},
) {
    sealed class Item {
        object Bookmarks : Item()
        object History : Item()
        object Downloads : Item()
        object Extensions : Item()
        data class SyncAccount(val accountState: AccountState) : Item()

        /**
         * A button item to open up the settings page of FxA, shown up in mozilla online builds.
         */
        object ManageAccountAndDevices : Item()
        object WhatsNew : Item()
        object Help : Item()
        object CustomizeHome : Item()
        object Settings : Item()
        object Quit : Item()
        object ReconnectSync : Item()
        data class DesktopMode(val checked: Boolean) : Item()
        object SetDefaultBrowser : Item()

    }

    private val primaryTextColor = ThemeManager.resolveAttribute(R.attr.textPrimary, context)
    private val syncDisconnectedColor =
        ThemeManager.resolveAttribute(R.attr.syncDisconnected, context)
    private val syncDisconnectedBackgroundColor =
        context.getColorFromAttr(R.attr.syncDisconnectedBackground)

    private val accountManager = FenixAccountManager(context)

    // 'Reconnect' and 'Quit' items aren't needed most of the time, so we'll only create the if necessary.
    private val reconnectToSyncItem by lazy {
        BrowserMenuHighlightableItem(
            context.getString(R.string.sync_reconnect),
            R.drawable.ic_sync_disconnected,
            iconTintColorResource = syncDisconnectedColor,
            textColorResource = primaryTextColor,
            highlight = BrowserMenuHighlight.HighPriority(
                backgroundTint = syncDisconnectedBackgroundColor,
                canPropagate = false,
            ),
            isHighlighted = { true },
        ) {
            onItemTapped.invoke(Item.ReconnectSync)
            ReportManager.getInstance().report("home_menu_sync_reconnect")
        }
    }

    private val quitItem by lazy {
        BrowserMenuImageText(
            context.getString(R.string.delete_browsing_data_on_quit_action),
            R.drawable.mozac_ic_quit,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.Quit)
            ReportManager.getInstance().report("home_menu_qiut")
        }
    }

    private fun syncSignInMenuItem(): BrowserMenuImageText? {
        val syncItemTitle =
            if (context.components.backgroundServices.accountManagerAvailableQueue.isReady()) {
                accountManager.accountProfileEmail ?: context.getString(R.string.sync_menu_sync_and_save_data)
            } else {
                null
            }

        return when (syncItemTitle) {
            null -> null
            else -> {
                BrowserMenuImageText(
                    syncItemTitle,
                    R.drawable.ic_signed_out,
                    primaryTextColor,
                ) {
                    onItemTapped.invoke(Item.SyncAccount(accountManager.accountState))
                    ReportManager.getInstance().report("home_menu_sync_and_save_data")
                }
            }
        }
    }

    val desktopItem = BrowserMenuImageSwitch(
        imageResource = R.drawable.ic_desktop,
        label = context.getString(R.string.browser_menu_desktop_site),
        initialState = { context.settings().openNextTabInDesktopMode },
    ) { checked ->
        onItemTapped.invoke(Item.DesktopMode(checked))
        ReportManager.getInstance().report("home_menu_desktop_site")
    }

    @Suppress("ComplexMethod")
    private fun coreMenuItems(): List<BrowserMenuItem> {
        val settings = context.components.settings

        val bookmarksItem = BrowserMenuImageText(
            context.getString(R.string.library_bookmarks),
            R.drawable.ic_bookmark_list,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.Bookmarks)
            ReportManager.getInstance().report("home_menu_bookmarks")
        }

        val historyItem = BrowserMenuImageText(
            context.getString(R.string.library_history),
            R.drawable.ic_history,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.History)
            ReportManager.getInstance().report("home_menu_history")
        }

        val downloadsItem = BrowserMenuImageText(
            context.getString(R.string.library_downloads),
            R.drawable.ic_download,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.Downloads)
            ReportManager.getInstance().report("home_menu_downloads")
        }

        val extensionsItem = BrowserMenuImageText(
            context.getString(R.string.browser_menu_add_ons),
            R.drawable.ic_addons_extensions,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.Extensions)
            ReportManager.getInstance().report("home_menu_add_ons")
        }

        val manageAccountAndDevicesItem = SimpleBrowserMenuItem(
            context.getString(R.string.browser_menu_manage_account_and_devices),
            textColorResource = primaryTextColor,
        ) {
            onItemTapped.invoke(Item.ManageAccountAndDevices)
            ReportManager.getInstance().report("home_menu_manage_account_and_devices")
        }

//        val whatsNewItem = BrowserMenuHighlightableItem(
//            context.getString(R.string.browser_menu_whats_new),
//            R.drawable.ic_whats_new,
//            iconTintColorResource = primaryTextColor,
//            highlight = BrowserMenuHighlight.LowPriority(
//                notificationTint = getColor(context, R.color.fx_mobile_icon_color_information),
//            ),
//            isHighlighted = { WhatsNew.shouldHighlightWhatsNew(context) },
//        ) {
//            onItemTapped.invoke(Item.WhatsNew)
//            ReportManager.getInstance().report("home_menu_whats_new")
//        }

//        val helpItem = BrowserMenuImageText(
//            context.getString(R.string.browser_menu_help),
//            R.drawable.mozac_ic_help,
//            primaryTextColor,
//        ) {
//            onItemTapped.invoke(Item.Help)
//            ReportManager.getInstance().report("home_menu_help")
//        }

        val customizeHomeItem = BrowserMenuImageText(
            context.getString(R.string.browser_menu_customize_home_1),
            R.drawable.ic_customize,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.CustomizeHome)
            ReportManager.getInstance().report("home_menu_menu_customize_home")
        }

        // Use nimbus to set the icon and title.
        val nimbusValidation = FxNimbus.features.nimbusValidation.value()
        val settingsItem = BrowserMenuImageText(
            nimbusValidation.settingsTitle,
            R.drawable.mozac_ic_settings,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.Settings)
            ReportManager.getInstance().report("home_menu_settings")
        }

        // Only query account manager if it has been initialized.
        // We don't want to cause its initialization just for this check.
        val accountAuthItem =
            if (context.components.backgroundServices.accountManagerAvailableQueue.isReady() &&
                context.components.backgroundServices.accountManager.accountNeedsReauth()
            ) {
                reconnectToSyncItem
            } else {
                null
            }

        val setDefaultBrowserItem = BrowserMenuImageText(
            context.getString(R.string.preferences_set_as_default_browser),
            R.drawable.max_ic_default_browser_menu,
            primaryTextColor,
        ) {
            onItemTapped.invoke(Item.SetDefaultBrowser)
            ReportManager.getInstance().report("home_menu_set_default_browser")
        }

        val menuItems = listOfNotNull(
            bookmarksItem,
            historyItem,
            downloadsItem,
            extensionsItem,
//            syncSignInMenuItem(),
            accountAuthItem,
            if (Config.channel.isMozillaOnline) manageAccountAndDevicesItem else null,
            BrowserMenuDivider(),
            desktopItem,
            BrowserMenuDivider(),
//            whatsNewItem,
//            helpItem,
            customizeHomeItem,
            if (settings.isDefaultBrowserBlocking()) null else setDefaultBrowserItem,
            settingsItem,
            if (settings.shouldDeleteBrowsingDataOnQuit) quitItem else null,
        ).also { items ->
            items.getHighlight()?.let { onHighlightPresent(it) }
        }

        return menuItems
    }

    init {
        val menuItems = coreMenuItems()

        // Report initial state.
        onMenuBuilderChanged(BrowserMenuBuilder(menuItems))

        // Observe account state changes, and update menu item builder with a new set of items.
        context.components.backgroundServices.accountManagerAvailableQueue.runIfReadyOrQueue {
            // This task isn't relevant if our parent fragment isn't around anymore.
            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                return@runIfReadyOrQueue
            }
            context.components.backgroundServices.accountManager.register(
                object : AccountObserver {
                    override fun onAuthenticationProblems() {
                        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            onMenuBuilderChanged(
                                BrowserMenuBuilder(
                                    menuItems,
                                ),
                            )
                        }
                    }

                    override fun onAuthenticated(account: OAuthAccount, authType: AuthType) {
                        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            onMenuBuilderChanged(
                                BrowserMenuBuilder(
                                    menuItems,
                                ),
                            )
                        }
                    }

                    override fun onLoggedOut() {
                        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            onMenuBuilderChanged(
                                BrowserMenuBuilder(
                                    menuItems,
                                ),
                            )
                        }
                    }
                },
                lifecycleOwner,
            )
        }
    }
}
