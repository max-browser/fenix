package org.mozilla.fenix.home.homead

import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.max.browser.core.data.local.sp.MaxAdSettings
import com.max.browser.core.utils.DeviceUtil
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.databinding.HomeNativeAdItemBinding
import org.mozilla.fenix.home.homead.interactor.HomeAdInteractor

class HomeNativeAdViewHolder(
    private val binding: HomeNativeAdItemBinding,
    private val viewLifecycleOwner: LifecycleOwner,
    private val appStore: AppStore,
    private val interactor: HomeAdInteractor,
) : RecyclerView.ViewHolder(binding.root), DefaultLifecycleObserver {

    init {
        viewLifecycleOwner.lifecycle.addObserver(this)
    }

    companion object {
        val LAYOUT_ID = R.layout.home_native_ad_item
    }

    private var nativeAd: NativeAd? = null

    fun bind() {
        binding.apply {
            interactor.onGetHomeNativeAd(
                nativeAdOptions = NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build(),
                getNativeAdCallback = { nativeAd ->
                    destroy()
                    this@HomeNativeAdViewHolder.nativeAd = nativeAd
                    itemView.layoutParams.width = DeviceUtil.getScreenWidth(itemView.context)

                    ivNativeAd.setImageDrawable(nativeAd.icon?.drawable)

                    tvAdTag.isVisible = nativeAd.headline != null
                    tvTitle.isVisible = nativeAd.headline != null
                    tvTitle.text = nativeAd.headline

                    tvBody.isVisible = nativeAd.body != null
                    tvBody.text = nativeAd.body

                    mvNativeAd.isVisible = nativeAd.mediaContent != null
                    mvNativeAd.mediaContent = nativeAd.mediaContent

                    vNativeAd.setOnClickListener { }

                    nav.apply {
                        iconView = ivNativeAd
                        headlineView = tvTitle
                        bodyView = tvBody
                        mediaView = mvNativeAd
                        callToActionView = vNativeAd
                        setNativeAd(nativeAd)
                    }

                },
            )
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        destroy()
    }

    fun destroy() {
        nativeAd?.destroy()
    }
}
