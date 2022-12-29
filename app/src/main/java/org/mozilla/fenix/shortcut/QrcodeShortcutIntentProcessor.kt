package org.mozilla.fenix.shortcut

import android.content.Intent
import android.os.Bundle
import com.max.browser.core.ReportManager
import mozilla.components.feature.intent.processing.IntentProcessor
import mozilla.components.support.utils.SafeIntent

class QrcodeShortcutIntentProcessor : IntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent): Boolean {
        val safeIntent = SafeIntent(intent)
        when (safeIntent.action) {
            ACTION_OPEN_QRCODE_SCANNER -> {
                ReportManager.getInstance().report(
                    "quick_menu_click",
                    Bundle().apply {
                        putString("type", "qr_scan")
                    },
                )
            }
            else -> return false
        }
        // return true
        // Do not process anything, just report event.
        return false
    }

    companion object {
        const val ACTION_OPEN_QRCODE_SCANNER = "com.max.browser.core.OPEN_QRCODE_SCANNER"
    }
}
