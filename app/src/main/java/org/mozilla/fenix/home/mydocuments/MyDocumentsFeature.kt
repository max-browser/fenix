/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.mydocuments

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.max.browser.core.ReportManager
import kotlinx.coroutines.*
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.OnNeedToRequestPermissions
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.ktx.android.content.isPermissionGranted
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.settings


class MyDocumentsFeature(
    private val context: Context,
    private val appStore: AppStore,
    private val scope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val myDocumentsUseCase: MyDocumentsUseCase,
) : LifecycleAwareFeature, PermissionsFeature {

    override val onNeedToRequestPermissions: OnNeedToRequestPermissions = { }

    companion object {
        val PERMISSIONS_BEFORE_API_28 = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private var job: Job? = null

    override fun start() {
        job = scope.launch(ioDispatcher) {
            val uriString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val key =
                    context.getPreferenceKey(R.string.pref_key_directory_access_permission_uri)
                context.settings().preferences.getString(key, "") ?: ""

            } else {
                ""
            }

            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uriString != ""
            } else {
                context.isPermissionGranted(PERMISSIONS_BEFORE_API_28.asIterable())
            }

            val result = if (hasPermission) {
                myDocumentsUseCase.queryDocument(context, uriString).also {
                    ReportManager.getInstance().report(
                        "queried_document_count",
                        Bundle().apply {
                            putString("count", it.size.toString())
                        },
                    )
                }

            } else {
                emptyList<MyDocumentsItem>()
            }

            updateState(hasPermission, result)
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

    override fun onPermissionsResult(permissions: Array<String>, grantResults: IntArray) {
        start()
    }
}
