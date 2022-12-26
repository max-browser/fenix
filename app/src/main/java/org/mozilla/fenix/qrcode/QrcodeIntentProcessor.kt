/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.qrcode

import android.content.Intent
import android.util.Log
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import mozilla.components.feature.intent.processing.IntentProcessor
import org.mozilla.fenix.HomeActivity

class QrcodeIntentProcessor() : IntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent): Boolean {
        try {
            val task = Firebase.dynamicLinks
                .getDynamicLink(intent)
            task.result.link?.let { deepLink ->
                val isQrcodeScannerLink = QRCODE_SCANNER_LINKS.contains(deepLink.toString())
                intent.putExtra(HomeActivity.OPEN_TO_QRCODE_SCANNER, isQrcodeScannerLink)
                return isQrcodeScannerLink
            }
        } catch (e: Exception) {
        }

        return false
    }

    companion object {
        val QRCODE_SCANNER_LINKS =
            arrayOf(
                "https://www.maxbrowser.co/qrcode-scanner",
                "https://maxbrowser.co/qrcode-scanner",
            )
    }
}
