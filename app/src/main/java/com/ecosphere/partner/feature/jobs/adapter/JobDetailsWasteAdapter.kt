package com.ecosphere.partner.feature.jobs.adapter

import android.content.res.ColorStateList
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecosphere.partner.databinding.RowWasteBinding
import com.ecosphere.partner.feature.jobs.`interface`.CapturePhotoUi
import com.ecosphere.partner.feature.jobs.`interface`.PhotoActionListener
import com.ecosphere.partner.feature.jobs.`interface`.WasteActionListener
import com.ecosphere.partner.feature.jobs.model.WasteCollectionUi

class JobDetailsWasteAdapter(
    private val listener: WasteActionListener
) : RecyclerView.Adapter<JobDetailsWasteAdapter.ViewHolder>() {

    private val items = mutableListOf<WasteCollectionUi>()

    init {
        setHasStableIds(true)
    }

    fun submitItems(data: List<WasteCollectionUi>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun getItems(): List<WasteCollectionUi> = items.toList()

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position]
            .wasteType
            .name
            .hashCode()
            .toLong()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = RowWasteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    inner class ViewHolder(
        private val binding: RowWasteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var weightWatcher: TextWatcher? = null

        private val photoAdapter by lazy {

            CapturePhotoAdapter(

                object : PhotoActionListener {

                    override fun onAddClicked() {

                        if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                            return

                        listener.onAddPhoto(
                            bindingAdapterPosition
                        )
                    }

                    override fun onRemoveClicked(
                        photo: CapturePhotoUi.Photo
                    ) {

                        if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                            return

                        listener.onRemovePhoto(
                            bindingAdapterPosition,
                            photo
                        )
                    }
                }
            )
        }

        init {

            binding.rvPhoto.apply {

                layoutManager =
                    LinearLayoutManager(
                        context,
                        RecyclerView.HORIZONTAL,
                        false
                    )

                adapter = photoAdapter
                itemAnimator = null
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
                setHasFixedSize(true)
            }
        }

        fun bind(
            item: WasteCollectionUi
        ) = with(binding) {

            tvWasteType.text = item.wasteType.displayName
            tvWasteType.setTextColor(
                ContextCompat.getColor(
                    root.context,
                    item.wasteType.color
                )
            )

            ivWasteColor.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        root.context,
                        item.wasteType.color
                    )
                )

            weightWatcher?.let {
                edtWeight.removeTextChangedListener(it)
            }

            edtWeight.setText(item.weight)

            weightWatcher = object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) = Unit

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    if (bindingAdapterPosition == RecyclerView.NO_POSITION)
                        return

                    if (item.weight != s.toString()) {

                        item.weight = s.toString()

                    }
                }
            }

            edtWeight.addTextChangedListener(
                weightWatcher
            )

            photoAdapter.submitList(
                buildPhotoItems(
                    item.images
                )
            )
        }

        fun clear() {

            weightWatcher?.let {

                binding.edtWeight.removeTextChangedListener(it)
            }

            weightWatcher = null
        }
    }


    override fun onViewRecycled(
        holder: ViewHolder
    ) {

        holder.clear()

        super.onViewRecycled(holder)
    }

    private fun buildPhotoItems(
        images: List<Uri>
    ): List<CapturePhotoUi> {

        val list = mutableListOf<CapturePhotoUi>()

        images.forEach {

            list.add(
                CapturePhotoUi.Photo(it)
            )
        }

        if (images.size < 5) {

            list.add(
                CapturePhotoUi.AddPhoto
            )
        }

        return list
    }

    fun addPhoto(
        position: Int,
        uri: Uri
    ) {

        if (position !in items.indices) return

        val waste = items[position]

        if (waste.images.size >= 5) return

        if (waste.images.contains(uri)) return

        waste.images.add(uri)
        notifyItemChanged(position)
    }
    fun replacePhoto(
        position: Int,
        imageIndex: Int,
        newUri: Uri
    ) {

        if (position !in items.indices)
            return

        val waste = items[position]

        if (imageIndex !in waste.images.indices)
            return

        waste.images[imageIndex] = newUri

        notifyItemChanged(position)
    }

    fun removePhoto(
        position: Int,
        uri: Uri
    ) {

        if (position !in items.indices)
            return

        val waste = items[position]

        if (waste.images.remove(uri)) {
            notifyItemChanged(position)
        }
    }

    fun updateWeight(
        position: Int,
        weight: String
    ) {

        if (position !in items.indices)
            return

        items[position].weight = weight
    }

    fun clearImages(
        position: Int
    ) {

        if (position !in items.indices)
            return

        items[position].images.clear()

        notifyItemChanged(position)
    }

    fun getItem(
        position: Int
    ): WasteCollectionUi? {

        return items.getOrNull(position)
    }
}