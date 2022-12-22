package org.mozilla.fenix.videodownloader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.max.browser.downloader.repository.DownloadRecordRepository
import com.max.browser.downloader.repository.VideoParserRepository
import com.max.browser.downloader.util.killProcess
import com.max.browser.downloader.util.safeLaunch
import com.max.browser.downloader.vo.DownloadRecord
import com.max.browser.downloader.vo.DownloadStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DownloadViewModel (
    private val repoDownloadRecord: DownloadRecordRepository,
    private val repoVideoParser: VideoParserRepository
) : ViewModel() {

    val allRecords: StateFlow<List<DownloadRecord>> = repoDownloadRecord.allRecord.stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )

    val allAudioRecords: StateFlow<List<DownloadRecord>> = repoDownloadRecord.allAudioRecord.stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )

    val allVideoRecords: StateFlow<List<DownloadRecord>> = repoDownloadRecord.allVideoRecord.stateIn(
        viewModelScope, SharingStarted.Lazily, listOf()
    )

    val allDownloading: StateFlow<List<DownloadRecord>> =
        repoDownloadRecord.allDownloading.stateIn(
            viewModelScope, SharingStarted.Lazily, listOf()
        )

    val allUnplayedMedia: StateFlow<List<DownloadRecord>> =
        repoDownloadRecord.allUnplayedMedia.stateIn(
            viewModelScope, SharingStarted.Lazily, listOf()
        )

    private fun updateStatus(
        record: DownloadRecord,
        status: Int,
        updatePauseCount: Boolean = false
    ) =
        viewModelScope.safeLaunch {
            repoDownloadRecord.updateStatus(
                record.url,
                record.formatId,
                record.videoHeightSize,
                record.abr,
                status,
                updatePauseCount
            )
        }

    fun markDownloadRecordBePlayed(record: DownloadRecord) = viewModelScope.safeLaunch {
        record.hasPlayed = true
        repoDownloadRecord.update(record)
    }

    fun markDownloadRecordBePlayed(fileFolder: String, fileName: String) = viewModelScope.safeLaunch {
        repoDownloadRecord.markDownloadRecordBePlayed(fileFolder, fileName)
    }

    fun pauseDownload(record: DownloadRecord) = viewModelScope.safeLaunch {
        updateStatus(record, DownloadStatus.PAUSE.value, true)
        killProcess(record.processId)
    }

    fun resumeDownload(
        worker: WorkManager,
        record: DownloadRecord,
    ) = viewModelScope.safeLaunch {
        repoVideoParser.getVideo(
            worker,
            record.url,
            record.title,
            record.formatId,
            record.videoHeightSize,
            record.abr,
            record.fileExtension,
            record.downloadFrom
        )
        updateStatus(record, DownloadStatus.DOWNLOADING.value)
    }

    fun cancelDownload(record: DownloadRecord) = viewModelScope.safeLaunch {
        repoDownloadRecord.cancelDownload(record)
        killProcess(record.processId)
    }

    fun deleteDownload(record: DownloadRecord) = viewModelScope.safeLaunch {
        repoDownloadRecord.deleteDownload(record)
    }


}
