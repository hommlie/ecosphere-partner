package com.ecosphere.partner.feature.patients.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecosphere.partner.databinding.ItemLoadingBinding

class PatientLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<
        PatientLoadStateAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding:
        ItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {

            binding.progressBar.visibility =
                if (loadState is LoadState.Loading) View.VISIBLE else View.GONE

            binding.btnRetry.visibility =
                if (loadState is LoadState.Error) View.VISIBLE else View.GONE

            binding.btnRetry.setOnClickListener {
                retry()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ViewHolder {

        val binding =
            ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }
}