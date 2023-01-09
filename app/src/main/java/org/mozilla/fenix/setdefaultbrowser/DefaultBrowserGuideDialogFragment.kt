package org.mozilla.fenix.setdefaultbrowser

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.max.browser.core.ReportManager
import org.mozilla.fenix.R
import org.mozilla.fenix.android.FenixDialogFragment
import org.mozilla.fenix.databinding.FragmentDefaultBrowserGuideDialogBinding
import org.mozilla.fenix.ext.openSetDefaultBrowserOption

class DefaultBrowserGuideDialogFragment : FenixDialogFragment() {

    private var _binding: FragmentDefaultBrowserGuideDialogBinding? = null

    private val binding get() = _binding!!
    override val gravity: Int get() = Gravity.BOTTOM
    override val layoutId: Int = R.layout.fragment_default_browser_guide_dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflateRootView(container)
        _binding = FragmentDefaultBrowserGuideDialogBinding.bind(rootView)
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
