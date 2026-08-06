package com.ecosphere.partner.feature.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ecosphere.partner.feature.patients.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {

    fun getPatientsPager():
            Flow<PagingData<Patient>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 3,
                initialLoadSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PatientPagingSource(repository)
            }
        ).flow.cachedIn(viewModelScope)
    }

}