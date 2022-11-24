/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.mydocuments

import android.net.Uri

data class MyDocumentsItem(
    val fileName: String,
    val uri: Uri,
    val mimeType: String,
    val size: Long,
    val lastModified: Long,
)
