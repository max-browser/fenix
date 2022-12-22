package org.mozilla.fenix.videodownloader

import android.os.Parcelable
import androidx.annotation.Keep
import com.max.browser.downloader.vo.DownloadRecord
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ItemDownloadList(
    val title: String? = "",
    val record: DownloadRecord? = null,
    val type: Int
): Parcelable
