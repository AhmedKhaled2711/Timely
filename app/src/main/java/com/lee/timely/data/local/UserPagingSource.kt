package com.lee.timely.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lee.timely.db.TimelyDao
import com.lee.timely.model.User

class UserPagingSource(
    private val dao: TimelyDao,
    private val groupId: Int,
    private val searchQuery: String = ""
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            val users = if (searchQuery.isBlank()) {
                dao.getUsersByGroupIdPaginated(
                    groupId = groupId,
                    limit = pageSize,
                    offset = page * pageSize
                )
            } else {
                dao.searchUsersByGroupId(
                    groupId = groupId,
                    query = searchQuery,
                    limit = pageSize,
                    offset = page * pageSize
                )
            }

            val nextKey = if (users.size < pageSize) {
                null
            } else {
                page + 1
            }

            val prevKey = if (page == 0) {
                null
            } else {
                page - 1
            }

            LoadResult.Page(
                data = users,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
} 