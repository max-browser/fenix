package org.mozilla.fenix.home.homead.interactor

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.mozilla.fenix.home.sessioncontrol.SessionControlInteractor

/**
 * Interface for recently saved bookmark related actions in the [SessionControlInteractor].
 */
interface HomeAdInteractor {

    fun onGetHomeNativeAd(
        nativeAdOptions: NativeAdOptions,
        getNativeAdCallback: (nativeAd: NativeAd) -> Unit,
    )

}
