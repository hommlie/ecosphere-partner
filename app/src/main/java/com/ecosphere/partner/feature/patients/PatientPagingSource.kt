package com.ecosphere.partner.feature.patients

import com.ecosphere.partner.core.network.ApiResult
import com.ecosphere.partner.core.common.BasePagingSource
import com.ecosphere.partner.feature.patients.model.Patient

class PatientPagingSource(
    private val repository: PatientRepository
) : BasePagingSource<Patient>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Patient> {

        val page = params.key ?: 1

        return when (
            val result = repository.getPatients(
                page,
                params.loadSize
            )
        ) {

            is ApiResult.Success -> {

                val data = result.data

                if (data == null) {

                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = null
                    )

                } else {

                    LoadResult.Page(

                        data = data.patients,

                        prevKey =
                            if (page == 1) null
                            else page - 1,

                        nextKey =
                            if (page < data.pagination.pages)
                                page + 1
                            else null
                    )
                }
            }

            else -> {
                LoadResult.Error(
                    Exception(result.errorMessage)
                )
            }
        }
    }
}