package com.ecosphere.partner.feature.jobs.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecosphere.partner.core.util.DateTimeUtils.toTime12Hour
import com.ecosphere.partner.databinding.RowPickupJobBinding
import com.ecosphere.partner.feature.jobs.model.PickupStatus
import com.ecosphere.partner.feature.jobs.model.PickupUi

class JobsAdapter(
    private val onCallClick : (PickupUi) -> Unit = {},
    private val onDirectionClick : (PickupUi) -> Unit = {},
    private val onViewClick : (PickupUi) -> Unit = {}
) : ListAdapter<PickupUi, JobsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowPickupJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: RowPickupJobBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickupUi) = with(binding) {

            tvCustomerName.text = item.customerName

            tvAllPickupId.text = "${item.pickupId} • ${item.pickupTime.toTime12Hour()}"

            tvPickupAddress.text = item.pickupAddress

            tvWasteType.text = item.wasteType.displayName

            tvDistance.text = "${item.distanceKm} km"

            tvWeight.text = "~${item.estimatedWeightKg} kg"

            bindWasteType(item)

            bindStatus(item)

            llCall.setOnClickListener {
                onCallClick(item)
            }

            llMap.setOnClickListener {
                onDirectionClick(item)
            }

            llStart.setOnClickListener {
                onViewClick(item)
            }
        }

        private fun bindWasteType(item: PickupUi) = with(binding) {

            val context = root.context
            val color = ContextCompat.getColor(context, item.wasteType.color)

            ivWasteType.imageTintList = ColorStateList.valueOf(color)
            tvWasteType.setTextColor(color)
        }

        private fun bindStatus(item: PickupUi) = with(binding) {

            val context = root.context
            val color = ContextCompat.getColor(context, item.status.color)

            tvStatus.text = item.status.displayName.uppercase()
            tvStatus.setTextColor(color)

            liveIndicator.setDotColor(color)
            liveIndicator.setRippleColor(color)

            when (item.status) {

                PickupStatus.PENDING,
                PickupStatus.STARTED -> {

                    liveIndicator.visibility = View.VISIBLE

                    cl2.visibility = View.VISIBLE
                    view1.visibility = View.VISIBLE

                    liveIndicator.start()
                }

                else -> {
                    liveIndicator.stop()
                    liveIndicator.visibility = View.INVISIBLE
                    cl2.visibility = View.GONE
                    view1.visibility = View.GONE
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PickupUi>() {
        override fun areItemsTheSame(oldItem: PickupUi, newItem: PickupUi): Boolean {
            return oldItem.pickupId == newItem.pickupId
        }
        override fun areContentsTheSame(oldItem: PickupUi, newItem: PickupUi): Boolean {
            return oldItem == newItem
        }
    }
}