package org.mozilla.fenix.videodownloader

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.max.browser.downloader.util.*
import com.max.browser.downloader.vo.DownloadStatus
import com.max.browser.videodownloader.R
import com.max.browser.videodownloader.databinding.ItemDownloadFinishBinding
import com.max.browser.videodownloader.databinding.ItemDownloadProgressBinding

class DownloadRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val values: MutableList<ItemDownloadList> = mutableListOf()
    var listener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DownloadListType.DOWNLOADING.value -> {
                DownloadingViewHolder(
                    ItemDownloadProgressBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            DownloadListType.FINISHED.value -> {
                DownloadedViewHolder(
                    ItemDownloadFinishBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                throw Exception("unknown view type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        holder.itemView.tag = item

        when (holder.itemViewType) {
            DownloadListType.DOWNLOADING.value -> {
                with(holder as DownloadingViewHolder) {
                    val record = item.record ?: return
                    //Timber.i("record progress: ${record.progress}")
                    glideForThumbnail(
                        holder.imageCover,
                        record.thumbnail,
                        record.url,
                        forDownloadList = false
                    )

                    holder.textTitle.text = record.title
                    holder.progress.progress = record.progress.toInt()

                    holder.textPercentage.text = String.format(
                        holder.itemView.context.getString(R.string.percentage),
                        (record.progress * 100 / 100).toString()
                    )

                    // default UI status
                    setTextToNormal(holder.textStatus)
                    holder.progressParsing.isVisible = false

                    when (record.status) {
                        DownloadStatus.DOWNLOADING.value -> {
                            holder.buttonControl.setImageResource(R.drawable.ic_progress_pause_24)
                            if (record.fileExtension == AUDIO_FORMAT_MP3 && record.progress == 100f) {
                                holder.textStatus.text =
                                    itemView.resources.getString(R.string.sound_optimization)
                                holder.textPercentage.visibility = View.INVISIBLE
                                holder.buttonControl.visibility = View.INVISIBLE
                            } else {
                                holder.textStatus.text =
                                    itemView.resources.getString(R.string.downloading)
                                holder.textPercentage.visibility = View.VISIBLE
                                holder.buttonControl.visibility = View.VISIBLE
                            }
                        }
                        DownloadStatus.PAUSE.value, DownloadStatus.EXPIRED.value -> {
                            holder.buttonControl.visibility = View.VISIBLE
                            holder.buttonControl.setImageResource(R.drawable.ic_progress_download_24)
                            with(holder.textStatus) {
                                text = resources.getString(R.string.paused)
                                setTextToPause(this)
                            }
                        }
                        DownloadStatus.FAILED.value -> {
                            holder.buttonControl.visibility = View.VISIBLE
                            holder.textStatus.text = itemView.resources.getString(R.string.error)
                        }
                        DownloadStatus.CONVERTING.value -> {
                            holder.textStatus.text =
                                itemView.resources.getString(R.string.converting)
                            holder.textPercentage.visibility = View.INVISIBLE
                            holder.buttonControl.visibility = View.GONE
                        }
                    }
                }
            }
            DownloadListType.FINISHED.value -> {
                with(holder as DownloadedViewHolder) {
                    val record = item.record ?: return

                    glideForThumbnail(
                        holder.imageCover,
                        record.thumbnail,
                        record.url,
                        forDownloadList = false
                    )

                    holder.textTitle.text = record.title
                    val fileSize = getFileFromRecord(record).length()
                    //"${getFullPathOfFile(record).length().getHumanReadableFileSizeMb()}MB"
                    holder.textSize.text = "${fileSize.getHumanReadableFileSizeMb()}MB"
                    var color: Int = itemView.context.resources.getColor(R.color.text)
                    var alpha = 1f
                    if (fileSize <= 0) {
                        color = itemView.context.resources.getColor(R.color.mine_shaft_30)
                        alpha = 0.5f
                    }
                    holder.imageCover.alpha = alpha
                    holder.textTitle.setTextColor(color)
                    holder.textSize.setTextColor(color)
                    holder.textExtension.setTextColor(color)
                    holder.textPixels.setTextColor(color)

                    holder.textExtension.text = record.fileExtension.uppercase()
                    holder.textPixels.text = when {
                        record.videoHeightSize != "0" -> {
                            "${record.videoHeightSize}P"
                        }
                        record.abr != 0 -> {
                            record.abr.abr2ReadableString()
                        }
                        else -> {
                            ""
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = values.size

    override fun getItemViewType(position: Int): Int {
        return values[position].type
    }

    fun setData(data: List<ItemDownloadList>, useNotify: Boolean = false) {
        if (useNotify) {
            values.clear()
            values.addAll(data)
            notifyDataSetChanged()
        } else {
            val diffCallback = DownloadDiffCallback(values.toList(), data)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            values.clear()
            values.addAll(data)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    private fun setTextToNormal(textView: TextView) {
        with(textView) {
            typeface = Typeface.DEFAULT
            this.setTextColor(resources.getColor(R.color.text))
        }
    }

    private fun setTextToPause(textView: TextView) {
        with(textView) {
            typeface = Typeface.DEFAULT_BOLD
            this.setTextColor(resources.getColor(R.color.primary_theme_color))
        }
    }

    inner class DownloadingViewHolder(binding: ItemDownloadProgressBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val imageCover = binding.itemImageCover
        val imageCancel = binding.itemImageCancel
        val textTitle = binding.itemTextTitle
        val progress = binding.itemProgress
        val textStatus = binding.itemTextStatus
        val textPercentage = binding.itemTextPercentage
        val buttonControl = binding.itemButtonControl
        val progressParsing = binding.itemProgressParsing

        init {
            //itemView.setOnClickListener(this)
            imageCancel.setOnClickListener(this)
            buttonControl.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                when (v) {
                    itemView -> {
                        listener?.onItemDownloadingClick(adapterPosition)
                    }
                    imageCancel -> {
                        listener?.onCancelClick(adapterPosition)
                    }
                    buttonControl -> {
                        listener?.onPauseResumeDelClick(adapterPosition)
                    }
                }
            }
        }
    }

    inner class DownloadedViewHolder(binding: ItemDownloadFinishBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val imageCover = binding.itemImageCover
        val textTitle = binding.itemTextTitle
        val textSize = binding.textSize
        val textExtension = binding.textExtension
        val textPixels = binding.textPixels
        private val buttonDel = binding.itemButtonControl

        init {
            itemView.clickWithDebounce{
                listener?.onItemClick(adapterPosition)
            }
            buttonDel.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                when (v) {
                    buttonDel -> {
                        listener?.onPauseResumeDelClick(adapterPosition)
                    }
                }
            }
        }
    }

    interface OnClickListener {
        fun onItemClick(position: Int)
        fun onItemDownloadingClick(position: Int)
        fun onPauseResumeDelClick(position: Int)
        fun onCancelClick(position: Int)
    }
}

class DownloadDiffCallback(
    private val oldList: List<ItemDownloadList>,
    private val newList: List<ItemDownloadList>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].record?.url == newList[newItemPosition].record?.url &&
                oldList[oldItemPosition].record?.formatId == newList[newItemPosition].record?.formatId &&
                oldList[oldItemPosition].record?.videoHeightSize == newList[newItemPosition].record?.videoHeightSize &&
                oldList[oldItemPosition].record?.abr == newList[newItemPosition].record?.abr &&
                oldList[oldItemPosition].record?.hasPlayed == newList[newItemPosition].record?.hasPlayed

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].record?.hasPlayed == newList[newItemPosition].record?.hasPlayed &&
        oldList[oldItemPosition].record == newList[newItemPosition].record

    // TODO: use payload to change download progress
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val diffBundle = Bundle()
        diffBundle.putBoolean("HAS_PLAYER", newList[newItemPosition].record?.hasPlayed ?: true)
        return diffBundle
    }
}

enum class DownloadListType(val value: Int) {
    DOWNLOADING(0),
    FINISHED(1)
}
