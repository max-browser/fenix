package org.mozilla.fenix.home.homead.controller

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.max.browser.core.data.remote.entities.ad.admob.AdMobNativeAd
import com.max.browser.core.domain.repository.AdRepository
import com.max.browser.core.feature.ad.AdPlacement

interface HomeAdController {
    fun handleGetHomeNativeAd(
        nativeAdOptions: NativeAdOptions,
        getNativeAdCallback: (nativeAd: NativeAd) -> Unit
    )
}

class DefaultHomeAdController(
    private val adRepository: AdRepository,
) : HomeAdController {
    override fun handleGetHomeNativeAd(
        nativeAdOptions: NativeAdOptions,
        getNativeAdCallback: (nativeAd: NativeAd) -> Unit
    ) {
        adRepository.getAdConfig(AdPlacement.AD_PLACEMENT_HOME_NATIVE)?.let { adConfig ->
            adRepository.loadAd(
                adConfig = adConfig,
                nativeAdOptions = nativeAdOptions,
                loadAdCallback = { isSuccessful: Boolean, ad: Any?, responseInfo: Any? ->
                    when (ad) {
                        is AdMobNativeAd -> {
                            ad.getAd()?.let { nativeAd ->
                                if (isSuccessful) {
                                    getNativeAdCallback(nativeAd)
                                }
                            }
                        }
                        else -> {
                        }
                    }
                },
            )
        }

    }


}
