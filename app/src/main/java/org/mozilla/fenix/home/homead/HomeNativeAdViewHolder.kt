package org.mozilla.fenix.home.homead

import android.text.format.DateUtils
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.max.browser.core.data.local.sp.MaxAdSettings
import com.max.browser.core.data.remote.entities.ad.AdConfig
import com.max.browser.core.feature.ad.AdPlacement
import com.max.browser.core.getAdConfigs
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
    private var isAdLoading = false

    fun bind() {
        val adConfig =
            getAdConfigs().find { it.placement == AdPlacement.AD_PLACEMENT_HOME_NATIVE } ?: return
        if (nativeAd != null) {
            return
        }
        nativeAd?.let {
            updateUi(it)

        } ?: run {
            val isOverDailyShowCount = checkOverDailyShowCount(adConfig)

            if (isOverDailyShowCount) {
                collapseItemView()
            } else {
                loadAd()
            }
        }

    }

    private fun checkOverDailyShowCount(adConfig: AdConfig): Boolean {
        val time = System.currentTimeMillis()
        val firstTime = MaxAdSettings.getInstance().homeNativeAdFirstTimeOfDailyShowing
        val duration = time - firstTime
        if (duration >= DateUtils.DAY_IN_MILLIS) {
            MaxAdSettings.getInstance().homeNativeAdDailyShowCount = 0
        }

        val dailyShowCount = MaxAdSettings.getInstance().homeNativeAdDailyShowCount
        return dailyShowCount >= adConfig.showCondition.dailyShowCount
    }

    private fun collapseItemView() {
        itemView.layoutParams.height = 0
    }

    private fun loadAd() {
        if (isAdLoading) {
            return
        }
        isAdLoading = true
        interactor.onGetHomeNativeAd(
            nativeAdOptions = NativeAdOptions.Builder()
                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE).build(),
            getNativeAdCallback = { nativeAd ->
                this@HomeNativeAdViewHolder.nativeAd = nativeAd
                this@HomeNativeAdViewHolder.isAdLoading = false
                updateUi(nativeAd)

                val showCount = MaxAdSettings.getInstance().homeNativeAdDailyShowCount
                if (showCount == 0) {
                    MaxAdSettings.getInstance().homeNativeAdFirstTimeOfDailyShowing =
                        System.currentTimeMillis()
                }
                MaxAdSettings.getInstance().homeNativeAdDailyShowCount = showCount + 1
            },
        )
    }

    private fun updateUi(nativeAd: NativeAd) {
        binding.apply {
            itemView.layoutParams.width = DeviceUtil.getScreenWidth(itemView.context)
            itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            ivNativeAd.setImageDrawable(nativeAd.icon?.drawable)

            tvAdTag.isVisible = nativeAd.headline != null
            tvTitle.isVisible = nativeAd.headline != null
            tvTitle.text = nativeAd.headline

            tvBody.isVisible = nativeAd.body != null
            tvBody.text = nativeAd.body

            mvNativeAd.isVisible = nativeAd.mediaContent != null
            mvNativeAd.mediaContent = nativeAd.mediaContent

//                    vNativeAd.setOnClickListener { }

            nav.apply {
                iconView = ivNativeAd
                headlineView = tvTitle
                bodyView = tvBody
                mediaView = mvNativeAd
//                        callToActionView = vNativeAd
                setNativeAd(nativeAd)
            }

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
