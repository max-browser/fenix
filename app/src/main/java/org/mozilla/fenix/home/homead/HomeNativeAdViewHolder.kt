package org.mozilla.fenix.home.homead

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.max.browser.core.utils.DeviceUtil
import kotlinx.coroutines.flow.map
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.fenix.R
import org.mozilla.fenix.components.AppStore
import org.mozilla.fenix.databinding.HomeNativeAdItemBinding
import org.mozilla.fenix.ext.isSystemInDarkTheme
import org.mozilla.fenix.home.homead.interactor.HomeAdInteractor

class HomeNativeAdViewHolder(
    private val binding: HomeNativeAdItemBinding,
    private val viewLifecycleOwner: LifecycleOwner,
    private val appStore: AppStore,
    private val interactor: HomeAdInteractor,
) : RecyclerView.ViewHolder(binding.root), DefaultLifecycleObserver {

    init {
        viewLifecycleOwner.lifecycle.addObserver(this)

        appStore.flowScoped(viewLifecycleOwner) { flow ->
            flow.map { state -> state.wallpaperState }.ifChanged().collect { currentState ->
                val context = itemView.context
                var backgroundColor =
                    ContextCompat.getColor(context, R.color.fx_mobile_layer_color_2)

                currentState.runIfWallpaperCardColorsAreAvailable { cardColorLight, cardColorDark ->
                    backgroundColor = if (context.isSystemInDarkTheme()) {
                        cardColorDark
                    } else {
                        cardColorLight
                    }
                }

                binding.cvNativeAd.setCardBackgroundColor(backgroundColor)
            }
        }

    }

    companion object {
        val LAYOUT_ID = R.layout.home_native_ad_item
    }

    private var nativeAd: NativeAd? = null

    fun bind() {


        val wallpaperState = appStore.state.wallpaperState

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

                    vNativeAd.setOnClickListener {  }

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
