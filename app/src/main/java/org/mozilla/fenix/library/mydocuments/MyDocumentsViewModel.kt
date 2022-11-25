package org.mozilla.fenix.library.mydocuments

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.max.browser.core.base.BaseViewModel
import com.max.browser.core.domain.repository.QueryDocRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mozilla.components.support.ktx.android.content.isPermissionGranted
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.mydocuments.MyDocumentsFeature
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem


sealed class MyDocumentsUiState {
    object NoPermission : MyDocumentsUiState()
    object Loading : MyDocumentsUiState()
    object Empty : MyDocumentsUiState()
    data class Success(val items: List<MyDocumentsItem>) : MyDocumentsUiState()
}

class MyDocumentsViewModel(private val queryDocRepository: QueryDocRepository) : BaseViewModel() {

    private val _myDocumentsUiState =
        MutableStateFlow<MyDocumentsUiState>(MyDocumentsUiState.NoPermission)
    val uiState: StateFlow<MyDocumentsUiState> = _myDocumentsUiState


    fun queryDocuments(context: Context) {
        launchDataLoad {
            _myDocumentsUiState.value = MyDocumentsUiState.Loading

            val uriString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val key =
                    context.getPreferenceKey(R.string.pref_key_directory_access_permission_uri)
                context.settings().preferences.getString(key, "")

            } else {
                Uri.fromFile(Environment.getRootDirectory()).toString()
            }

            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uriString != ""
            } else {
                context.isPermissionGranted(MyDocumentsFeature.PERMISSIONS_BEFORE_API_28.asIterable())
            }

            if (hasPermission) {
                val items = queryDocRepository.queryDocumentFromDocumentTree(
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
                }.sortedByDescending {
                    it.lastModified
                }

                if (items.isEmpty()) {
                    _myDocumentsUiState.value = MyDocumentsUiState.Empty

                } else {
                    _myDocumentsUiState.value = MyDocumentsUiState.Success(items)
                }

            } else {
                _myDocumentsUiState.value = MyDocumentsUiState.NoPermission
            }

        }
    }
}
