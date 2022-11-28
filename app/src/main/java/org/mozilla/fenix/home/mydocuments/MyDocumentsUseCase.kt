package org.mozilla.fenix.home.mydocuments

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import com.max.browser.core.MaxBrowserConstant
import com.max.browser.core.doc.QueryData
import com.max.browser.core.domain.repository.PdfCacheRepository
import com.max.browser.core.domain.repository.QueryDocRepository

class MyDocumentsUseCase(
    private val queryDocRepository: QueryDocRepository,
    private val pdfCacheRepository: PdfCacheRepository,
) {

    suspend fun queryDocument(context: Context, uriString: String): List<MyDocumentsItem> {
        val result: List<MyDocumentsItem>
        val items = ArrayList<MyDocumentsItem>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            items.addAll(queryDocumentFromDocumentTree(context, uriString))
        } else {
            items.addAll(queryDocumentFromPath())
        }

        items.addAll(queryDocumentCache(context, items))

        result = items.sortedByDescending {
            it.lastModified
        }

        return result
    }

    private fun queryDocumentFromDocumentTree(
        context: Context,
        uriString: String,
    ): List<MyDocumentsItem> {
        return queryDocRepository.queryDocumentFromDocumentTree(
            context,
            Uri.parse(uriString),
            "application/pdf",
        ).map {
            MyDocumentsItem(
                it.sourceFileName,
                it.sourceUri,
                it.mimeType,
                it.size,
                it.lastModified,
            )
        }
    }

    private fun queryDocumentFromPath(): Collection<MyDocumentsItem> {
        val queryDataList = ArrayList<QueryData>()

        val pathList = ArrayList<String>()

        pathList.add(Environment.getExternalStorageDirectory().absolutePath)
        pathList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
        pathList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath)

        MaxBrowserConstant.WHATSAPP_NAMES.forEach {
            pathList.add("${Environment.getExternalStorageDirectory()}/$it/Media/$it Documents")
        }

        pathList.forEach {
            queryDataList.addAll(queryDocRepository.queryDocumentFromPath(it))
        }

        return queryDataList.distinctBy {
            it.sourceUri.toString()
        }.map {
            MyDocumentsItem(
                it.sourceFileName,
                it.sourceUri,
                it.mimeType,
                it.size,
                it.lastModified,
            )
        }

    }

    private suspend fun queryDocumentCache(
        context: Context,
        items: ArrayList<MyDocumentsItem>,
    ): List<MyDocumentsItem> {
        return pdfCacheRepository.getPdfCache().filter { pdfCache ->
            var hasFound = false
            for (item in items) {
                if (pdfCache.sourceUri == item.uri.toString()) {
                    hasFound = true
                }
            }
            return@filter !hasFound
        }.filter {
            return@filter pdfCacheRepository.getPdfCacheFile(context, it.md5).exists()
        }.map {
            val cacheFile = pdfCacheRepository.getPdfCacheFile(context, it.md5)

            MyDocumentsItem(
                it.sourceFileName,
                Uri.fromFile(cacheFile),
                "application/pdf",
                cacheFile.length(),
                it.createdDate,
            )
        }
    }

}
