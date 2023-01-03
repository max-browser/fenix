package org.mozilla.fenix.dynamiclink

import android.content.Intent
import mozilla.components.feature.intent.processing.IntentProcessor
import org.mozilla.fenix.HomeActivity

class DynamicLinkIntentProcessor : IntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent): Boolean {
        val deepLink = intent.data
        when {
            QRCODE_SCANNER_LINKS.contains(deepLink.toString()) -> {
                intent.putExtra(HomeActivity.OPEN_TO_QRCODE_SCANNER, true)
                return true
            }
            else -> {}
        }
        return false
    }

    companion object {
        val QRCODE_SCANNER_LINKS = arrayOf(
            "https://www.maxbrowser.co/qrcode-scanner",
            "https://maxbrowser.co/qrcode-scanner",
        )
    }
}
