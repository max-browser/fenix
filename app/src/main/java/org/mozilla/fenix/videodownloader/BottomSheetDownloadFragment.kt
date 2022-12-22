package org.mozilla.fenix.videodownloader

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.max.browser.downloader.ui.dialog.DlpFormatChooserViewModel
import com.max.browser.downloader.util.*
import com.max.browser.downloader.vo.VideoFormat
import com.max.browser.downloader.vo.VideoInfo
import com.max.browser.videodownloader.R
import com.max.browser.videodownloader.databinding.FragmentDlpFormatChooserBinding
import com.max.browser.videodownloader.databinding.ItemBottomsheetVideoformatsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class BottomSheetDownloadFragment : DialogFragment() {

    private var _binding: FragmentDlpFormatChooserBinding? = null
    private val binding get() = _binding!!
    private val args: BottomSheetDownloadFragmentArgs by navArgs()
    private val viewModel: DlpFormatChooserViewModel by viewModel()

    private lateinit var videoInfo:VideoInfo
    private var currentVideoFormat: VideoFormat? = null
    private var layoutList = mutableListOf<View>()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDlpFormatChooserBinding.inflate(LayoutInflater.from(context))

        val navController = findNavController()
        videoInfo = args.argVideoInfo

        viewModel.isOkToDownload.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    navController.navigateUp()
                    /*if (args.argNav2DownloadPage) {
                        val request = NavDeepLinkRequest.Builder
                            .fromUri("vd://downloader.cc/download?argFileType=$fileType".toUri())
                            .build()
                        navController.navigate(request)
                    } else {
                        navController.safeNavigate(WebViewFragmentDirections.actionWebviewToDownload())
                    }*/
                }
            }
        }

        Glide.with(this@BottomSheetDownloadFragment)
            .load(videoInfo.thumbnail)
            .centerInside()
            .placeholder(R.drawable.img_placeholder)
            .into(binding.imageCover)
        binding.textTitle.text = videoInfo.title

        videoInfo.videoFormats?.apply {
            var videoCount = 0
            this.forEach { videoFormat ->
                ItemBottomsheetVideoformatsBinding.inflate(
                    requireActivity().layoutInflater,
                    binding.root,
                    false
                ).apply {
                    this.root.tag = videoFormat.formatId
                    layoutList.add(this.root)
                    val fileExist = checkMediaFile(
                        videoInfo.title,
                        videoFormat.ext,
                        if (videoFormat.height == 0) "${videoFormat.abr}K" else "${videoFormat.height}P"
                    ).exists()

                    with(textFormatTitle) {
                        text =
                            if (videoFormat.height == 0)
                                "${videoFormat.abr.abr2ReadableString()} - ${videoFormat.ext.uppercase()}"
                            else
                                "${videoFormat.height}P - ${videoFormat.ext.uppercase()}"
                        if (fileExist) {
                            setTextColor(resources.getColor(R.color.mine_shaft_77))
                        }
                    }
                    if (videoFormat.fileSize > 0) {
                        with(textFormatSize) {
                            isVisible = true
                            text = if (fileExist) {
                                setTextColor(resources.getColor(R.color.mine_shaft_77))
                                resources.getText(R.string.downloaded)
                            } else {
                                "${videoFormat.fileSize.getHumanReadableFileSizeMb()} MB"
                            }
                        }
                    }


                    with(root) {
                        if (fileExist) {
                            this.setBackgroundResource(R.drawable.selector_item_format_bg_file_existed)
                            alpha = 0.25f
                        }

                        layoutParams = ConstraintLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        id = View.generateViewId()
                        tag = videoFormat
                        setOnClickListener {

                            if (fileExist) {
                                toast(R.string.resource_downloaded)
                                return@setOnClickListener
                            }
                            currentVideoFormat = tag as VideoFormat
                            Log.i("VD", "VD videoFormat id:${currentVideoFormat?.formatId}")
                            clearAllView()
                            this.isSelected = !this.isSelected
                            if (this.isSelected) {
                                imageChecker.visibility = View.VISIBLE
                            } else {
                                imageChecker.visibility = View.INVISIBLE
                            }
                            binding.textDownload.isEnabled = true
                        }
                    }

                    binding.layoutConstraint.addView(this.root)
                    binding.flowFormatsVideo.addView(this.root)
                    videoCount++
                }
            }

            if (videoCount < 3) {
                binding.flowFormatsVideo.setWrapMode(Flow.WRAP_CHAIN)
            }
        }

        binding.textDownload.clickWithDebounce {
            startDownload()
        }

        binding.imageClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        val builder = requireActivity().buildAlertDialog().apply {
            setView(binding.root)
        }
        return builder.create()
    }

    private fun clearAllView() {
        layoutList.forEach {
            it.isSelected = false
            it.findViewById<ImageView>(R.id.image_checker).visibility = View.INVISIBLE
        }
    }


    private fun startDownload() {
        /*val downloadReportItem = DownloadReportItem(
            source = args.argDownloadSource,
            selectedType = mediaFormat,
            isMusic = isMusic,
            domain = domain,
            url = videoInfo.originUrl
        )
        Timber.d("start download reportItem: $downloadReportItem")
        AppEventReporter.reportDownloadStart(downloadReportItem)*/
        currentVideoFormat?.let {
            viewModel.getVideo(
                WorkManager.getInstance(requireActivity()),
                videoInfo, it
            )
        }
    }

}
