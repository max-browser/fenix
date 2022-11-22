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
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.R
import org.mozilla.fenix.android.FenixDialogFragment
import org.mozilla.fenix.databinding.FragmentSetDefaultBrowserDialogSheetBinding
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.settings


fun Context.setAfterUpdatingTheme() {
    val sp = PreferenceManager.getDefaultSharedPreferences(this)
    val key = getPreferenceKey(R.string.pref_key_after_updating_theme)
    sp.edit().putBoolean(key, true).apply()
}

fun Fragment.checkToShowSetDefaultBrowserSheetDialogFragment() {
    if (requireContext().settings().isDefaultBrowserBlocking()) {
        return
    }

    val sp = PreferenceManager.getDefaultSharedPreferences(context)
    var key = getPreferenceKey(R.string.pref_key_has_checked_setting_default_browser)
    val hasCheckedSettingsDefaultBrowser = sp.getBoolean(key, false)
    if (hasCheckedSettingsDefaultBrowser) {
        return
    } else {
        sp.edit().putBoolean(key, true).apply()
    }

    key = getPreferenceKey(R.string.pref_key_after_updating_theme)
    val isAfterUpdatingTheme = sp.getBoolean(key, false)
    if (isAfterUpdatingTheme) {
        sp.edit().putBoolean(key, false).apply()
        return
    }

    key = getPreferenceKey(R.string.pref_key_first_time_to_set_default_browser)
    val isFirstTimeToSetDefaultBrowser = sp.getBoolean(key, true)
    if (isFirstTimeToSetDefaultBrowser) {
        sp.edit().putBoolean(key, false).apply()
        return
    }

    findNavController().navigate(NavGraphDirections.actionGlobalSetDefaultBrowserSheetDialogFragment())
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
    tvTitle.text = getString(R.string.notification_default_browser_text, getString(R.string.app_name))
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
