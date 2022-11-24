/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.mydocuments

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.max.browser.core.domain.repository.QueryDocRepository
import kotlinx.coroutines.*
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.settings

@VisibleForTesting
internal const val MAX_RESULTS_TOTAL = 9

@VisibleForTesting
internal const val MIN_VIEW_TIME_OF_HIGHLIGHT = 10.0

@VisibleForTesting
internal const val MIN_FREQUENCY_OF_HIGHLIGHT = 4.0

class MyDocumentsFeature(
    private val context: Context,
    private val appStore: AppStore,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val queryDocRepository: QueryDocRepository,
) : LifecycleAwareFeature {

    private var job: Job? = null

    override fun start() {
        job = scope.launch(ioDispatcher) {
            val key = context.getPreferenceKey(R.string.pref_key_directory_access_permission_uri)
            val uri = context.settings().preferences.getString(key, "")
            val hasPermission = uri != ""
            val items = ArrayList<MyDocumentsItem>()
            if (hasPermission) {
                items.addAll(
                    queryDocRepository.queryDocumentFromDocumentTree(
                        context,
                        Uri.parse(uri),
                        "application/pdf",
                    ).map {
                        MyDocumentsItem(
                            it.sourceFileName,
                            it.sourceUri,
                            it.mimeType,
                            it.size,
                            it.lastModified,
                        )
                    }.sortedByDescending {
                        it.lastModified
                    },
                )
            }
            updateState(hasPermission, items)
        }
    }

    override fun stop() {
        job?.cancel()
    }

    private fun updateState(
        hasPermission: Boolean,
        myDocumentsItems: List<MyDocumentsItem>,
    ) {
        appStore.dispatch(
            AppAction.MyDocumentsChange(MyDocumentsItems(hasPermission, myDocumentsItems)),
        )
    }

    fun saveMediaDirectoryUri(directoryUri: Uri) {
        val key = context.getPreferenceKey(R.string.pref_key_directory_access_permission_uri)
        context.settings().preferences.edit().putString(key, directoryUri.toString()).apply()
    }
}
