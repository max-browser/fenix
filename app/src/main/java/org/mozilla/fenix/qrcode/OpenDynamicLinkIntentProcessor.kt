package org.mozilla.fenix.qrcode

import android.content.ComponentName
import android.content.Intent
import androidx.navigation.NavController
import com.max.browser.core.feature.qrcode.QrcodeActivity
import com.max.browser.core.feature.vpn.VpnActivity
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.home.intent.HomeIntentProcessor

class OpenDynamicLinkIntentProcessor(
    private val activity: HomeActivity,
) : HomeIntentProcessor {

    /**
     * Processes the given [Intent].
     *
     * @param intent The intent to process.
     * @return True if the intent was processed, otherwise false.
     */
    override fun process(intent: Intent, navController: NavController, out: Intent): Boolean {
        return when {
            intent.extras?.getBoolean(HomeActivity.OPEN_TO_QRCODE_SCANNER) == true -> {
                intent.component =
                    ComponentName(activity.packageName, QrcodeActivity::class.java.name)
                activity.startActivity(intent)
                true
            }
            intent.extras?.getBoolean(HomeActivity.OPEN_TO_VPN) == true -> {
                intent.component = ComponentName(activity.packageName, VpnActivity::class.java.name)
                activity.startActivity(intent)
                true
            }
            else -> {
                false
            }
        }

    }

}