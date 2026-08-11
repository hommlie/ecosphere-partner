package com.ecosphere.partner.feature.jobs.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ecosphere.partner.databinding.RowPhotoBinding
import com.ecosphere.partner.feature.jobs.`interface`.CapturePhotoUi
import com.ecosphere.partner.feature.jobs.`interface`.PhotoActionListener

class CapturePhotoAdapter(
    private val listener: PhotoActionListener
) : ListAdapter<CapturePhotoUi, RecyclerView.ViewHolder>(DiffCallback()) {

    init {
        setHasStableIds(true)
    }

    companion object {
        private const val TYPE_PHOTO = 0
        private const val TYPE_ADD = 1
    }

    override fun getItemId(position: Int): Long {
        return when (val item = getItem(position)) {
            is CapturePhotoUi.Photo -> item.uri.hashCode().toLong()
            CapturePhotoUi.AddPhoto -> Long.MAX_VALUE
        }
    }

    override fun getItemViewType(position: Int): Int {

        return when (getItem(position)) {
            is CapturePhotoUi.Photo -> TYPE_PHOTO
            CapturePhotoUi.AddPhoto -> TYPE_ADD
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val binding = RowPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return if (viewType == TYPE_PHOTO) {
            PhotoViewHolder(binding)
        } else {
            AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (holder) {

            is PhotoViewHolder -> holder.bind(
                getItem(position) as CapturePhotoUi.Photo,
                position
            )

            is AddViewHolder -> holder.bind()
        }
    }

    inner class AddViewHolder(
        private val binding: RowPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() = with(binding) {

            llPlaceHolder.visibility = View.VISIBLE
            ivClickedImage.visibility = View.GONE
            ivRemove.visibility = View.GONE

            root.setOnClickListener {
                listener.onAddClicked()
            }
        }
    }

    inner class PhotoViewHolder(
        private val binding: RowPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: CapturePhotoUi.Photo,
            position: Int
        ) = with(binding) {

            llPlaceHolder.visibility = View.GONE
            ivClickedImage.visibility = View.VISIBLE
            ivRemove.visibility = View.VISIBLE

            Glide.with(ivClickedImage)
                .load(item.uri)
                .centerCrop()
                .into(ivClickedImage)

            ivRemove.setOnClickListener {
                listener.onRemoveClicked(item)
            }
        }
    }

    private class DiffCallback :
        DiffUtil.ItemCallback<CapturePhotoUi>() {

        override fun areItemsTheSame(
            oldItem: CapturePhotoUi,
            newItem: CapturePhotoUi
        ): Boolean {

            return when {

                oldItem is CapturePhotoUi.Photo &&
                        newItem is CapturePhotoUi.Photo ->
                    oldItem.uri == newItem.uri

                oldItem is CapturePhotoUi.AddPhoto &&
                        newItem is CapturePhotoUi.AddPhoto ->
                    true

                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: CapturePhotoUi,
            newItem: CapturePhotoUi
        ): Boolean = oldItem == newItem
    }
}