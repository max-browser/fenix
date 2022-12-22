package org.mozilla.fenix.videodownloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.max.browser.videodownloader.R
import com.max.browser.videodownloader.databinding.FragmentDlpMediaBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.support.base.log.logger.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseDownloadFragment : Fragment(R.layout.fragment_dlp_media) {

    var _binding: FragmentDlpMediaBinding? = null
    val binding get() = _binding!!
    val downloadViewModel: DownloadViewModel by viewModel()
    val navController by lazy(LazyThreadSafetyMode.NONE) {
        this.findNavController()
    }
    var currentTabMode:Int? = TAB_VIDEO

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDlpMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.list.adapter = null
        //_binding = null
    }

    fun handleEmptyView(isEmpty: Boolean) {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            e.printStackTrace()
        }
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val visibility = if (isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if (activity == null || !isAdded) {
                Logger.info("fragment is detach")
                return@launch
            }

            if (isEmpty) {
                binding.layoutNoContent.visibility = View.VISIBLE
                binding.list.visibility = View.GONE
            } else {
                binding.layoutNoContent.visibility = View.GONE
                binding.list.visibility = View.VISIBLE
            }
            binding.textHaveNotDownloaded.text = resources.getText(R.string.have_no_resource)
        }
    }


}
