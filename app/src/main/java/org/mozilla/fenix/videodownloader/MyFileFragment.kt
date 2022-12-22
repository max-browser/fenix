package org.mozilla.fenix.videodownloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.max.browser.downloader.util.clickWithDebounce
import com.max.browser.downloader.util.safeNavigate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.support.base.log.logger.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.BrowserFragmentDirections
import org.mozilla.fenix.databinding.FragmentMyFileBinding
import org.mozilla.fenix.ext.filterNotExistsOnDisk
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.ext.showToolbar
import org.mozilla.fenix.library.downloads.DownloadItem

class MyFileFragment : Fragment(R.layout.fragment_my_file) {

    private var _binding: FragmentMyFileBinding? = null
    private val binding get() = _binding!!
    private val args:MyFileFragmentArgs by navArgs()
    private val downloadViewModel: DownloadViewModel by viewModel()
    private var haFromNotification:Boolean = false
    private val navController by lazy(LazyThreadSafetyMode.NONE) {
        this.findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Logger.info("onCreateView")
        _binding = FragmentMyFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.info("Frank argNav2MediaFilePage:${args.argNav2MediaFilePage}")
        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.allRecords.collectLatest {
                val count = String.format(requireContext().getString(
                    R.string.title_file_count, it.size))
                if (isAdded) {
                    binding.mediaFilesTextCount.text = count
                }
            }
        }

        binding.layoutAllFiles.clickWithDebounce {
            navController.safeNavigate(
                BrowserFragmentDirections.actionGlobalDownloadsFragment(),
            )
        }

        binding.layoutMediaFile.clickWithDebounce {
            navController.safeNavigate(
                BrowserFragmentDirections.actionGlobalDlpMediaFragment(),
            )
        }

        if (args.argNav2MediaFilePage) {
            if (haFromNotification) {
                return
            }
            navController.safeNavigate(
                BrowserFragmentDirections.actionGlobalDlpMediaFragment(),
            )
            haFromNotification = true
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.title_my_files))
        checkAllFilesCount()
    }

    private fun checkAllFilesCount() {
        val items = provideDownloads(requireComponents.core.store.state)
        Logger.info("download page item size:${items.size}")
        val allFilesCount = String.format(requireContext().getString(
            R.string.title_file_count, items.size))
        binding.allFilesTextCount.text = allFilesCount
    }

    @VisibleForTesting
    internal fun provideDownloads(state: BrowserState): List<DownloadItem> {
        return state.downloads.values
            .distinctBy { it.fileName }
            .sortedByDescending { it.createdTime } // sort from newest to oldest
            .map {
                DownloadItem(
                    id = it.id,
                    url = it.url,
                    fileName = it.fileName,
                    filePath = it.filePath,
                    size = it.contentLength?.toString() ?: "0",
                    contentType = it.contentType,
                    status = it.status,
                )
            }.filter {
                it.status == DownloadState.Status.COMPLETED
            }.filterNotExistsOnDisk()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
