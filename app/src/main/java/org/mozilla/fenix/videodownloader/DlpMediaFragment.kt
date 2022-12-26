package org.mozilla.fenix.videodownloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkManager
import com.max.browser.core.feature.reader.video.openVideoPlayerByDlp
import com.max.browser.core.feature.reader.video.openVideoReaderByFilePath
import com.max.browser.downloader.report.Action
import com.max.browser.downloader.report.AppEventReporter
import com.max.browser.downloader.report.ClickType
import com.max.browser.downloader.report.PageType
import com.max.browser.downloader.ui.dialog.DeleteDownloadDialogFragment
import com.max.browser.downloader.ui.dialog.DeleteTaskDialogFragment
import com.max.browser.downloader.util.getFileFromRecord
import com.max.browser.downloader.util.toast
import com.max.browser.downloader.vo.DownloadRecord
import com.max.browser.downloader.vo.DownloadStatus
import com.max.browser.videodownloader.R
import com.max.browser.videodownloader.databinding.FragmentDlpMediaBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.ext.showToolbar
import java.io.File

const val ARG_NOTIFICATION_FILE_PATH = "ARG_FILE_PATH"
const val ARG_NEW_SONGS_PATH = "ARG_NEW_SONGS_PATH"

class DlpMediaFragment : BaseDownloadFragment(),
    DownloadRecyclerViewAdapter.OnClickListener {

    private val data = mutableListOf<ItemDownloadList>()
    private val adapter = DownloadRecyclerViewAdapter().apply {
        listener = this@DlpMediaFragment
    }

    companion object {
        fun newInstance(mode: Int, argNotificationFilePath: String?, newSongFilePath:String?): DlpMediaFragment {
            val fragment = DlpMediaFragment()
            val bundle = Bundle().apply {
                putInt(KEY_MODE, mode)
                putString(ARG_NOTIFICATION_FILE_PATH, argNotificationFilePath)
                putString(ARG_NEW_SONGS_PATH, newSongFilePath)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDlpMediaBinding.inflate(inflater, container, false)
        AppEventReporter.reportDownloadFilePage(type = Action.SHOW, page = PageType.MEDIA_FILE)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.title_media_file))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        currentTabMode = arguments?.getInt(KEY_MODE, TAB_VIDEO)
        subscribeObserversDownload()
    }

    private fun showList(list: List<DownloadRecord>, useNotify:Boolean = false) {
        data.clear()
        if (list.isEmpty()) {
            handleEmptyView(true)
        } else {
            handleEmptyView(false)
            val downloading = mutableListOf<ItemDownloadList>()
            val finish = mutableListOf<ItemDownloadList>()

            list.forEach {
                if (it.status == DownloadStatus.FINISHED.value) {
                    finish.add(
                        ItemDownloadList(
                            record = it,
                            type = DownloadListType.FINISHED.value
                        )
                    )
                } else {
                    downloading.add(
                        ItemDownloadList(
                            record = it,
                            type = DownloadListType.DOWNLOADING.value
                        )
                    )
                }
            }
            data.addAll(downloading)
            data.addAll(finish)
            handleEmptyView(false)
        }
        adapter.setData(data, useNotify)
    }

    private fun subscribeObserversDownload() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                when (currentTabMode) {
                    TAB_ALL -> {
                        downloadViewModel.allRecords.collectLatest {
                            showList(it,true)
                        }
                    }
                    TAB_MUSIC -> {
                        downloadViewModel.allAudioRecords.collectLatest {
                            showList(it,true)
                        }
                    }
                    TAB_VIDEO -> {
                        downloadViewModel.allVideoRecords.collectLatest {
                            showList(it, true)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(savedInstanceState: Bundle?) {
        with(binding.list) {
            adapter = this@DlpMediaFragment.adapter
            setHasFixedSize(true)
        }
    }

    override fun onItemDownloadingClick(position: Int) {
        Logger.info("onItemDownloadingClick position:$position")
        with(data[position]) {
            val data = this.record ?: return
            when (data.status) {
                DownloadStatus.PAUSE.value -> {
                    activity?.let {
                        downloadViewModel.resumeDownload(WorkManager.getInstance(it), data)
                    } ?: throw IllegalStateException("Activity cannot be null")
                }
                else -> {}
            }
        }
    }

    override fun onItemClick(position: Int) {
        var finalPos = position
        if (position < 0 || position >= data.size) {
            finalPos = 0
        }
        try {
            with(data[finalPos]) {
                val data = this.record ?: return
                val file = getFileFromRecord(data)
                if (file.exists().not()) {
                    toast(R.string.file_has_been_deleted)
                    return@with
                }
                clickFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clickFile(file: File) {
        Logger.info("clickFile file path:${file.absolutePath}")
        AppEventReporter.reportDownloadFilePage(type = Action.CLICK, page = PageType.MEDIA_FILE, action = ClickType.PLAY)
        requireContext().openVideoPlayerByDlp(file.absolutePath)
    }

    override fun onPauseResumeDelClick(position: Int) {
        with(data[position]) {
            val data = this.record ?: return
            when (data.status) {
                DownloadStatus.DOWNLOADING.value -> {
                    AppEventReporter.reportDownloadFilePage(type = Action.CLICK, page = PageType.MEDIA_FILE, action = ClickType.PAUSE)
                    downloadViewModel.pauseDownload(data)
                }
                DownloadStatus.PAUSE.value -> {
                    activity?.let {
                        AppEventReporter.reportDownloadFilePage(type = Action.CLICK, page = PageType.MEDIA_FILE, action = ClickType.CONTINUE_DOWNLOAD)
                        downloadViewModel.resumeDownload(WorkManager.getInstance(it), data)
                    } ?: throw IllegalStateException("Activity cannot be null")
                }
                DownloadStatus.FINISHED.value -> {
                    DeleteDownloadDialogFragment.newInstance(data.url).apply {
                        listener = object : DeleteDownloadDialogFragment.DeleteDownloadListener {
                            override fun onConfirmDeleteDownload(url: String) {
                                this@DlpMediaFragment.data.find { data.url == url }?.let {
                                    AppEventReporter.reportDownloadFilePage(type = Action.CLICK, page = PageType.MEDIA_FILE, action = ClickType.REMOVE_FILE)
                                    downloadViewModel.deleteDownload(data)
                                }
                            }
                        }
                    }.show(childFragmentManager, null)
                }
                DownloadStatus.FAILED.value -> {
                }
                else -> {
                }
            }
        }
    }

    override fun onCancelClick(position: Int) {
        with(data[position]) {
            val data = this.record ?: return
            DeleteTaskDialogFragment.newInstance(data.processId).apply {
                listener = object : DeleteTaskDialogFragment.DeleteTaskListener {
                    override fun onConfirmDeleteTask(processId: String) {
                        this@DlpMediaFragment.data.find { data.processId == processId }?.let {
                            AppEventReporter.reportDownloadFilePage(type = Action.CLICK, page = PageType.MEDIA_FILE, action = ClickType.REMOVE_TASK)
                            downloadViewModel.cancelDownload(data)
                        }
                    }
                }
            }.show(childFragmentManager, null)
        }
    }

}
