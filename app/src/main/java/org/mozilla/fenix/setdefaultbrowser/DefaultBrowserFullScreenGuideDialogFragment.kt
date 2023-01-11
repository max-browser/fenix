package org.mozilla.fenix.setdefaultbrowser

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.max.browser.core.ReportManager
import com.max.browser.core.base.BaseDialogFragment
import com.max.browser.core.data.local.sp.MaxBrowserSettings
import org.mozilla.fenix.R
import org.mozilla.fenix.databinding.FragmentDefaultBrowserGuideDialogBinding
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import timber.log.Timber

class DefaultBrowserFullScreenGuideDialogFragment : BaseDialogFragment() {

    private var _binding: FragmentDefaultBrowserGuideDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = DefaultBrowserFullScreenGuideDialogFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDefaultBrowserGuideDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getScreenName(): String {
        return "default_browser_full_screen_dialog"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MaxBrowserSettings.getInstance().fullScreenStyleDefaultBrowserSettingDialogHadShowed = true
        ReportManager.getInstance().screenView("full_default_browser_dialog", javaClass.simpleName)
        ReportManager.getInstance().report("show_full_default_browser_dialog")
        ReportManager.getInstance().report(
            "full_default_browser_set",
            Bundle().apply {
                putString("class", "show")
            },
        )
        initUi()
        setListeners()
    }

    private fun initUi() {
        binding.apply {
            bNext.text =
                getString(R.string.notification_default_browser_text, getString(R.string.app_name))
            tvCancel.paint.flags = Paint.UNDERLINE_TEXT_FLAG; //下劃線
            tvCancel.paint.isAntiAlias = true;//抗鋸齒
        }
    }

    private fun setListeners() {

        binding.apply {
            tvCancel.setOnClickListener {
                dismissAllowingStateLoss()
                ReportManager.getInstance().report("click_full_default_browser_dialog_cancel")
            }
            bNext.setOnClickListener {
                dismissAllowingStateLoss()
                activity?.openSetDefaultBrowserOption()
                ReportManager.getInstance().report("click_full_default_browser_dialog_next")
                ReportManager.getInstance().report(
                    "full_default_browser_set",
                    Bundle().apply {
                        putString("class", "next")
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
