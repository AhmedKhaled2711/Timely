package com.lee.timely.data.local

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lee.timely.db.TimelyDao
import com.lee.timely.model.User

class UserPagingSource(
    private val dao: TimelyDao,
    private val groupId: Int,
    private val searchQuery: String = "",
    private val month: Int? = null,
    private val isPaid: Boolean = true
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
            val isNumericSearch = searchQuery.matches(Regex("\\d+"))
            
            Log.d("UserPagingSource", "Loading page $page with pageSize $pageSize")
            Log.d("UserPagingSource", "Group ID: $groupId, Month: $month, isPaid: $isPaid, Search: '$searchQuery'")
            Log.d("UserPagingSource", "Is numeric search: $isNumericSearch")

            val users = when {
                month != null && searchQuery.isNotBlank() -> {
                    Log.d("UserPagingSource", "Searching with month filter and payment status")
                    if (isNumericSearch) {
                        // Search by UID with month and payment status
                        dao.searchUsersByUidAndMonthAndPaymentStatus(
                            groupId = groupId,
                            uid = searchQuery.toInt(),
                            month = month,
                            isPaid = isPaid,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                            Log.d("UserPagingSource", "Found ${it.size} users by UID with month filter") 
                        }
                    } else {
                        // Regular text search with month and payment status
                        dao.searchUsersByGroupIdAndMonthAndPaymentStatus(
                            groupId = groupId,
                            query = searchQuery,
                            month = month,
                            isPaid = isPaid,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                            Log.d("UserPagingSource", "Found ${it.size} users with text search and month filter") 
                        }
                    }
                }
                month != null -> {
                    Log.d("UserPagingSource", "Filtering users by month and payment status")
                    dao.getUsersByGroupIdAndMonthAndPaymentStatus(
                        groupId = groupId,
                        month = month,
                        isPaid = isPaid,
                        limit = pageSize,
                        offset = page * pageSize
                    ).also { 
                        Log.d("UserPagingSource", "Found ${it.size} users with month filter") 
                    }
                }
                searchQuery.isNotBlank() -> {
                    Log.d("UserPagingSource", "Searching users with query")
                    if (isNumericSearch) {
                        // Search by UID only
                        dao.searchUsersByUid(
                            groupId = groupId,
                            uid = searchQuery.toInt(),
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                            Log.d("UserPagingSource", "Found ${it.size} users by UID") 
                        }
                    } else {
                        // Regular text search
                        dao.searchUsersByGroupId(
                            groupId = groupId,
                            query = searchQuery,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                            Log.d("UserPagingSource", "Found ${it.size} users with text search") 
                        }
                    }
                }
                else -> {
                    Log.d("UserPagingSource", "Loading all users for group")
                    dao.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        limit = pageSize,
                        offset = page * pageSize
                    ).also { 
                        Log.d("UserPagingSource", "Found ${it.size} users in group") 
                    }
                }
            }

            val nextKey = if (users.size < pageSize) {
                Log.d("UserPagingSource", "No more pages after this one")
                null
            } else {
                Log.d("UserPagingSource", "Next page: ${page + 1}")
                page + 1
            }

            LoadResult.Page(
                data = users,
                prevKey = if (page == 0) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Log.e("UserPagingSource", "Error loading users", e)
            LoadResult.Error(e)
        }
    }
} 