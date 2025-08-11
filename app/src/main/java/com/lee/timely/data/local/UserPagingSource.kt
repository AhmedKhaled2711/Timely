package com.lee.timely.data.local

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lee.timely.model.User

private const val TAG = "UserPagingSource"
private const val STARTING_PAGE_INDEX = 0

class UserPagingSource(
    private val dao: TimelyDao,
    private val groupId: Int,
    private val searchQuery: String = ""
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            // Start refresh at page 1 if undefined
            val page = params.key ?: STARTING_PAGE_INDEX
            val pageSize = params.loadSize.coerceAtLeast(1)

            Log.d(TAG, "Loading page $page with pageSize $pageSize, groupId: $groupId, query: '$searchQuery'")

            val users = try {
                if (searchQuery.isBlank()) {
                    dao.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        limit = pageSize,
                        offset = page * pageSize
                    ) ?: emptyList()
                } else {
                    dao.searchUsersByGroupId(
                        groupId = groupId,
                        query = searchQuery,
                        limit = pageSize,
                        offset = page * pageSize
                    ) ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users from database: ${e.message}", e)
                return LoadResult.Error(e)
            }

            Log.d(TAG, "Loaded ${users.size} users")

            // Calculate next and previous keys
            val nextKey = if (users.isEmpty() || users.size < pageSize) {
                null
            } else {
                page + 1
            }

            val prevKey = if (page == STARTING_PAGE_INDEX) {
                null
            } else {
                (page - 1).takeIf { it >= STARTING_PAGE_INDEX }
            }

            LoadResult.Page(
                data = users,
                prevKey = prevKey,
                nextKey = nextKey,
                itemsBefore = if (page == STARTING_PAGE_INDEX) 0 else page * pageSize,
                itemsAfter = 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in paging source: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val TAG = "UserPagingSource"
    }
}