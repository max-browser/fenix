package org.mozilla.fenix.setdefaultbrowser

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.max.browser.core.RemoteConfigKey
import com.max.browser.core.RemoteConfigManager
import com.max.browser.core.ReportManager
import com.max.browser.core.data.local.sp.MaxBrowserSettings
import org.mozilla.fenix.NavGraphDirections
import org.mozilla.fenix.R
import org.mozilla.fenix.android.FenixDialogFragment
import org.mozilla.fenix.databinding.FragmentDefaultBrowserDialogSheetBinding
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.settings
import java.util.concurrent.TimeUnit


fun Activity.checkToShowDefaultBrowserSheetDialogFragment() {
    if (settings().isDefaultBrowserBlocking()) {
        return
    }
    val browserGroup = RemoteConfigManager.getInstance()
        .getConfig<String>(RemoteConfigKey.DEFAULT_BROWSER_DIALOG_SETTING_GROUP)

    val hasCheckedSettingsDefaultBrowser =
        MaxBrowserSettings.getInstance().hasCheckedSettingDefaultBrowserAfterColdStartingApp
    // 每一次的冷啟動開始，只會 check 一次是否展示彈窗
    if (hasCheckedSettingsDefaultBrowser) {
        return
    } else {
        MaxBrowserSettings.getInstance().hasCheckedSettingDefaultBrowserAfterColdStartingApp = true
    }

    val isAfterUpdatingTheme = MaxBrowserSettings.getInstance().isAfterUpdatingTheme
    if (isAfterUpdatingTheme) {
        // 因為更新主題而造成判斷此方法，所以需要跳過
        MaxBrowserSettings.getInstance().isAfterUpdatingTheme = false
        return
    }

    // Remove after v1.0.7
//    val isFirstTime = MaxBrowserSettings.getInstance().isFirstTimeToShowDefaultBrowserDialog
//    if (isFirstTime) {
//        // 首次不展示
//        MaxBrowserSettings.getInstance().isFirstTimeToShowDefaultBrowserDialog = false
//        return
//    }

    val firstTime = MaxBrowserSettings.getInstance().firstTimeOfShowingDefaultBrowserDialog


    val time = System.currentTimeMillis()
    if (time - firstTime > TimeUnit.DAYS.toMillis(1)) {
        // 超過一天，重置累計次數
        MaxBrowserSettings.getInstance().countOfShowingDefaultBrowserDialog = 0
    }

    val count = MaxBrowserSettings.getInstance().countOfShowingDefaultBrowserDialog
    val maxCount = RemoteConfigManager.getInstance()
        .getConfig<Int>(RemoteConfigKey.MAX_COUNT_OF_SHOWING_SET_DEFAULT_BROWSER_DIALOG)

    if (count < maxCount) {
        when (browserGroup) {
            "A" -> {
                openSetDefaultBrowserOption()
            }
            "B" -> {
                // do nothing
            }
            else -> {
                findNavController(R.id.container).navigate(NavGraphDirections.actionGlobalDefaultBrowserSheetDialogFragment())
            }
        }
        MaxBrowserSettings.getInstance().countOfShowingDefaultBrowserDialog = count + 1
        if (count == 0) {
            MaxBrowserSettings.getInstance().firstTimeOfShowingDefaultBrowserDialog = time
        }
    }

}

class DefaultBrowserSheetDialogFragment : FenixDialogFragment() {

    private var _binding: FragmentDefaultBrowserDialogSheetBinding? = null

    private val binding get() = _binding!!
    override val gravity: Int get() = Gravity.BOTTOM
    override val layoutId: Int = R.layout.fragment_default_browser_dialog_sheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflateRootView(container)
        _binding = FragmentDefaultBrowserDialogSheetBinding.bind(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ReportManager.getInstance().screenView("default_browser_dialog", javaClass.simpleName)
        ReportManager.getInstance().report("show_default_browser_dialog")
        ReportManager.getInstance().report(
            "default_browser_set",
            Bundle().apply {
                putString("action", "show")
            },
        )
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
                ReportManager.getInstance().report("click_default_browser_dialog_cancel")
            }
            bNext.setOnClickListener {
                dismissAllowingStateLoss()
                activity?.openSetDefaultBrowserOption()
                ReportManager.getInstance().report("click_default_browser_dialog_next")
                ReportManager.getInstance().report(
                    "default_browser_set",
                    Bundle().apply {
                        putString("action", "next")
                    },
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
