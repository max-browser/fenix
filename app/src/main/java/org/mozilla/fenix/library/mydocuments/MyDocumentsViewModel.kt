package org.mozilla.fenix.library.mydocuments

import android.content.Context
import android.os.Build
import com.max.browser.core.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mozilla.components.support.ktx.android.content.isPermissionGranted
import org.mozilla.fenix.R
import org.mozilla.fenix.ext.getPreferenceKey
import org.mozilla.fenix.ext.settings
import org.mozilla.fenix.home.mydocuments.MyDocumentsFeature
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem
import org.mozilla.fenix.home.mydocuments.MyDocumentsUseCase


sealed class MyDocumentsUiState {
    object NoPermission : MyDocumentsUiState()
    object Loading : MyDocumentsUiState()
    object Empty : MyDocumentsUiState()
    data class Success(val items: List<MyDocumentsItem>) : MyDocumentsUiState()
}

class MyDocumentsViewModel(
    private val myDocumentsUseCase: MyDocumentsUseCase,
) : BaseViewModel() {

    private val _myDocumentsUiState =
        MutableStateFlow<MyDocumentsUiState>(MyDocumentsUiState.NoPermission)
    val uiState: StateFlow<MyDocumentsUiState> = _myDocumentsUiState


    fun queryDocuments(context: Context) {
        launchDataLoad {
            _myDocumentsUiState.value = MyDocumentsUiState.Loading

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
                context.isPermissionGranted(MyDocumentsFeature.PERMISSIONS_BEFORE_API_28.asIterable())
            }


            if (hasPermission) {
                val result = myDocumentsUseCase.queryDocument(context, uriString)

                if (result.isEmpty()) {
                    _myDocumentsUiState.value = MyDocumentsUiState.Empty

                } else {
                    _myDocumentsUiState.value = MyDocumentsUiState.Success(result)
                }

            } else {
                _myDocumentsUiState.value = MyDocumentsUiState.NoPermission
            }

        }
    }
}
