package org.mozilla.fenix.home.mydocuments.view

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import mozilla.components.lib.state.ext.observeAsComposableState
import org.mozilla.fenix.R
import org.mozilla.fenix.components.components
import org.mozilla.fenix.compose.ComposeViewHolder
import org.mozilla.fenix.home.mydocuments.MyDocumentsItems
import org.mozilla.fenix.home.mydocuments.interactor.MyDocumentsInteractor
import org.mozilla.fenix.wallpapers.WallpaperState

class MyDocumentsViewHolder(
    composeView: ComposeView,
    viewLifecycleOwner: LifecycleOwner,
    private val interactor: MyDocumentsInteractor,
) : ComposeViewHolder(composeView, viewLifecycleOwner) {

    init {
        val horizontalPadding =
            composeView.resources.getDimensionPixelSize(R.dimen.home_item_horizontal_margin)
        composeView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    @Composable
    override fun Content() {
        val myDocumentsItems = components.appStore
            .observeAsComposableState { state -> state.myDocumentsItems }
        val wallpaperState = components.appStore
            .observeAsComposableState { state -> state.wallpaperState }.value ?: WallpaperState.default

        MyDocuments(
            myDocumentsItems = myDocumentsItems.value ?: MyDocumentsItems(),
            backgroundColor = wallpaperState.wallpaperCardColor,
            onMyDocumentsItemClick = {myDocumentsItem, position ->
                interactor.onMyDocumentsItemClicked(myDocumentsItem)
            },
            onGetPermissionClick = {
                interactor.onMyDocumentsGetPermissionClicked()
            }
        )
    }

    companion object {
        val LAYOUT_ID = View.generateViewId()
    }
}
