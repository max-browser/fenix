package org.mozilla.fenix.home.mydocuments

data class MyDocumentsItems(
    val hasPermission: Boolean = false,
    val items: List<MyDocumentsItem> = emptyList(),
)
