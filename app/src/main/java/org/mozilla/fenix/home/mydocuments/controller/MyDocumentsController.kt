package org.mozilla.fenix.home.mydocuments.controller

import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.state.store.BrowserStore
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.home.HomeFragmentDirections
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem

interface MyDocumentsController {
    fun handleMyDocumentsShowAllClicked()
    fun handleMyDocumentsItemClicked(myDocumentsItem: MyDocumentsItem)
    fun handleMyDocumentsGetPermissionClicked()
}

class DefaultMyDocumentsController(
    private val store: BrowserStore,
    private val appStore: AppStore,
    private val navController: NavController,
    private val scope: CoroutineScope,
    private val onGetMyDocumentsPermission: () -> Unit,
    private val onOpenPdf: (uri: Uri) -> Unit,

    ) : MyDocumentsController {

    override fun handleMyDocumentsShowAllClicked() {
        dismissSearchDialogIfDisplayed()
        navController.navigate(
            HomeFragmentDirections.actionGlobalMyDocumentsFragment()
        )
    }

    override fun handleMyDocumentsItemClicked(myDocumentsItem: MyDocumentsItem) {
        onOpenPdf(myDocumentsItem.uri)
    }

    override fun handleMyDocumentsGetPermissionClicked() {
        onGetMyDocumentsPermission()
    }

    private fun dismissSearchDialogIfDisplayed() {
        if (navController.currentDestination?.id == R.id.searchDialogFragment) {
            navController.navigateUp()
        }
    }
}
