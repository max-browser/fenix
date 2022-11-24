package org.mozilla.fenix.home.mydocuments.interactor

import org.mozilla.fenix.home.mydocuments.MyDocumentsItem

interface MyDocumentsInteractor {
    fun onMyDocumentsShowAllClicked()
    fun onMyDocumentsItemClicked(myDocumentsItem: MyDocumentsItem)
    fun onMyDocumentsGetPermissionClicked()
}
