package org.mozilla.fenix.library.mydocuments

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.max.browser.core.ext.toFileSizeString
import org.mozilla.fenix.databinding.MyDocumentsListItemBinding
import org.mozilla.fenix.home.mydocuments.MyDocumentsItem


class MyDocumentsAdapter(
    private val lifecycle: Lifecycle,
    private val onMyDocumentItemClicked: (myDocumentsItem: MyDocumentsItem) -> Unit,

    ) : ListAdapter<MyDocumentsItem, MyDocumentsAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<MyDocumentsItem>() {
        override fun areItemsTheSame(
            oldItem: MyDocumentsItem,
            newItem: MyDocumentsItem,
        ): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: MyDocumentsItem,
            newItem: MyDocumentsItem,
        ): Boolean {
            return oldItem == newItem
        }
    },
), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "MyDocumentsAdapter"
        const val VIEW_TYPE_NORMAL = 1
    }

    init {
        lifecycle.addObserver(this)
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_NORMAL -> MyDocumentsItemHolder(
                MyDocumentsListItemBinding.inflate(
                    inflater,
                    parent,
                    false,
                ),
                onMyDocumentItemClicked,
            )

            else -> EmptyHolder(parent.context)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is MyDocumentsItemHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class MyDocumentsItemHolder(
        private val binding: MyDocumentsListItemBinding,
        private val onMyDocumentItemClicked: (myDocumentsItem: MyDocumentsItem) -> Unit,
    ) : ViewHolder(binding.root) {
        fun bind(myDocumentsItem: MyDocumentsItem) {
            binding.apply {
                root.setOnClickListener {
                    onMyDocumentItemClicked(myDocumentsItem)
                }
                tvFileName.text = myDocumentsItem.fileName
                tvLastModified.text =
                    DateUtils.getRelativeTimeSpanString(myDocumentsItem.lastModified)
                tvFileSize.text = myDocumentsItem.size.toFileSizeString()
            }
        }
    }

    class EmptyHolder(context: Context) : ViewHolder(View(context))

}
