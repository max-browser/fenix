/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.qrcode

import android.content.ComponentName
import android.content.Intent
import androidx.navigation.NavController
import com.max.browser.core.feature.qrcode.QrcodeActivity
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.home.intent.HomeIntentProcessor

class OpenQrcodeScannerIntentProcessor(
    private val activity: HomeActivity,
) : HomeIntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return if (intent.extras?.getBoolean(HomeActivity.OPEN_TO_QRCODE_SCANNER) == true) {
            intent.component = ComponentName(activity.packageName, QrcodeActivity::class.java.name)
            activity.startActivity(intent)
            true
        } else {
            false
        }
    }

}
