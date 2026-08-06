package com.ecosphere.partner.core.common

import androidx.paging.PagingSource
import androidx.paging.PagingState

abstract class BasePagingSource<Value : Any> :
    PagingSource<Int, Value>() {

    override fun getRefreshKey(
        state: PagingState<Int, Value>
    ): Int? {

        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}