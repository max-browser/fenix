/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.setdefaultbrowser

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.max.browser.core.RemoteConfigKey
import com.max.browser.core.RemoteConfigManager
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.R
import org.mozilla.fenix.android.FenixDialogFragment
import org.mozilla.fenix.databinding.FragmentSetDefaultBrowserDialogSheetBinding
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.settings
import java.util.concurrent.TimeUnit


fun Context.setAfterUpdatingTheme() {
    val sp = PreferenceManager.getDefaultSharedPreferences(this)
    val key = getPreferenceKey(R.string.pref_key_is_after_updating_theme)
    sp.edit().putBoolean(key, true).apply()
}

fun Fragment.checkToShowSetDefaultBrowserSheetDialogFragment() {
    if (requireContext().settings().isDefaultBrowserBlocking()) {
        return
    }

    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    var key =
        getPreferenceKey(R.string.pref_key_has_checked_setting_default_browser_after_cold_starting_app)
    val hasCheckedSettingsDefaultBrowser = sp.getBoolean(key, false)
    // 每一次的冷啟動開始，只會 check 一次是否展示彈窗
    if (hasCheckedSettingsDefaultBrowser) {
        return
    } else {
        sp.edit().putBoolean(key, true).apply()
    }

    key = getPreferenceKey(R.string.pref_key_is_after_updating_theme)
    val isAfterUpdatingTheme = sp.getBoolean(key, false)
    if (isAfterUpdatingTheme) {
        // 因為更新主題而造成判斷此方法，所以需要跳過
        sp.edit().putBoolean(key, false).apply()
        return
    }

    key = getPreferenceKey(R.string.pref_key_is_first_time_to_show_set_default_browser_dialog)
    val isFirstTime = sp.getBoolean(key, true)
    if (isFirstTime) {
        // 首次不展示
        sp.edit().putBoolean(key, false).apply()
        return
    }

    key = getPreferenceKey(R.string.pref_key_first_time_of_showing_set_default_browser_dialog)
    val firstTime = sp.getLong(key, 0)

    key = getPreferenceKey(R.string.pref_key_count_of_showing_set_default_browser_dialog)

    val time = System.currentTimeMillis()
    if (time - firstTime > TimeUnit.DAYS.toSeconds(1)) {
        // 超過一天，重置累計次數
        sp.edit().putInt(key, 0).apply()
    }

    val count = sp.getInt(key, 0)
    val maxCount = RemoteConfigManager.getInstance()
        .getConfig<Int>(RemoteConfigKey.MAX_COUNT_OF_SHOWING_SET_DEFAULT_BROWSER_DIALOG)

    if (count < maxCount) {
        findNavController().navigate(NavGraphDirections.actionGlobalSetDefaultBrowserSheetDialogFragment())
        sp.edit().putInt(key, count + 1).apply()

        if (count == 0) {
            key =
                getPreferenceKey(R.string.pref_key_first_time_of_showing_set_default_browser_dialog)
            sp.edit().putLong(key, time).apply()
        }
    }

}

class SetDefaultBrowserSheetDialogFragment : FenixDialogFragment() {

    private var _binding: FragmentSetDefaultBrowserDialogSheetBinding? = null

    private val binding get() = _binding!!
    override val gravity: Int get() = Gravity.BOTTOM
    override val layoutId: Int = R.layout.fragment_set_default_browser_dialog_sheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflateRootView(container)
        _binding = FragmentSetDefaultBrowserDialogSheetBinding.bind(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        setListeners()
    }

    private fun initUi() {
        binding.apply {
            tvTitle.text =
                getString(R.string.notification_default_browser_text, getString(R.string.app_name))
        }

    }

    private fun setListeners() {

        binding.apply {
            bCancel.setOnClickListener {
                dismissAllowingStateLoss()
            }
            bNext.setOnClickListener {
                dismissAllowingStateLoss()
                activity?.openSetDefaultBrowserOption()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
