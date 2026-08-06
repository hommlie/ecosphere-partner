package com.ecosphere.partner.feature.patients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ecosphere.partner.core.common.toCapOnlyFirstLetter
import com.ecosphere.partner.databinding.RowPatientBinding
import com.ecosphere.partner.feature.patients.model.Patient

class PatientsAdapter : PagingDataAdapter<Patient, PatientsAdapter.PatientViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {

        val binding = RowPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class PatientViewHolder(private val binding: RowPatientBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(patient: Patient) {

            binding.tvPatientName.text = "${patient.firstName?.toCapOnlyFirstLetter()} ${patient.lastName?.toCapOnlyFirstLetter()}"

            binding.tvPatientAge.text = "Age : ${patient.age}    Gender : ${patient.gender?.toCapOnlyFirstLetter()}"

            binding.tvMrnId.text = patient.mrn

            binding.tvSymptoms.text = "Active Case : ${patient.cases?.firstOrNull()?.caseTitle?:""}"

        }
    }

    class DiffCallback :
        DiffUtil.ItemCallback<Patient>() {

        override fun areItemsTheSame(
            oldItem: Patient,
            newItem: Patient
        ) = oldItem.id == newItem.id


        override fun areContentsTheSame(
            oldItem: Patient,
            newItem: Patient
        ) = oldItem == newItem
    }
}