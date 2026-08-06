package com.ecosphere.partner.feature.attendance.adapter

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.toKm
import com.ecosphere.partner.core.util.DateTimeUtils.toTime12Hour
import com.ecosphere.partner.databinding.RowSessionsBinding
import com.ecosphere.partner.feature.tracking.model.TrackingStatus
import com.ecosphere.partner.feature.attendance.model.TrackingSessionUi

class AttendanceSummaryAdapter(
    private val onSessionClick: (TrackingSessionUi) -> Unit
)  : ListAdapter<TrackingSessionUi, AttendanceSummaryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: RowSessionsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrackingSessionUi) = with(binding) {

            tvSessionCount.text = "Session ${item.sessionNumber}"

            tvStartTime.text = item.startTime.toTime12Hour()
            tvEndTime.text = if(item.endTime == null) "- -" else item.endTime.toTime12Hour()
            tvDistance.text = item.totalDistance.toKm()

            when (item.status) {

                TrackingStatus.STARTED.name -> {

                    tvSessionStatus.text = "Running"

                    vAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.color_16A34A
                        )
                    )
                    clStatus.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.green))

                    clTopCard.background =
                        ContextCompat.getDrawable(
                            root.context,
                            R.drawable.bg_session_running
                        )

                    liveIndicator.visibility = View.VISIBLE
                    ivCompleted.visibility = View.GONE
                    liveIndicator.start()
                }

                TrackingStatus.STOPPED.name -> {

                    tvSessionStatus.text = "Completed"
                    vAccent.setBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.gray_text_color
                        )
                    )
                    clStatus.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            root.context,
                            R.color.gray_text_color
                        )
                    )

                    clTopCard.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.white))

                    liveIndicator.visibility = View.GONE

                    ivCompleted.visibility = View.VISIBLE
                }
            }
            mcvRoot.setOnClickListener {
                onSessionClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            RowSessionsBinding.inflate(
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
        Log.d("Adapter Check", "onBind position = $position")
        holder.bind(getItem(position))
    }

    private class DiffCallback :
        DiffUtil.ItemCallback<TrackingSessionUi>() {

        override fun areItemsTheSame(
            oldItem: TrackingSessionUi,
            newItem: TrackingSessionUi
        ) = oldItem.sessionId == newItem.sessionId

        override fun areContentsTheSame(
            oldItem: TrackingSessionUi,
            newItem: TrackingSessionUi
        ) = oldItem == newItem
    }
}