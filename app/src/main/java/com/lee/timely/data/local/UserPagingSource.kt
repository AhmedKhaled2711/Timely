package com.lee.timely.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lee.timely.domain.User
import com.lee.timely.util.AcademicYearUtils

class UserPagingSource(
    private val dao: TimelyDao,
    private val groupId: Int,
    private val searchQuery: String = "",
    private val month: Int? = null,
    private val isPaid: Boolean = true,
    private val academicYear: String = AcademicYearUtils.getCurrentAcademicYear()
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
            
            val users = when {
                month != null && searchQuery.isNotBlank() -> {
                    if (isNumericSearch) {
                        // Search by UID with month and payment status
                        dao.searchUsersByUidAndMonthAndPaymentStatus(
                            groupId = groupId,
                            uid = searchQuery.toInt(),
                            academicYear = academicYear,
                            month = month,
                            isPaid = isPaid,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                        }
                    } else {
                        // Regular text search with month and payment status
                        dao.searchUsersByGroupIdAndMonthAndPaymentStatus(
                            groupId = groupId,
                            academicYear = academicYear,
                            query = searchQuery,
                            month = month,
                            isPaid = isPaid,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                        }
                    }
                }
                month != null -> {
                    dao.getUsersByGroupIdAndMonthAndPaymentStatus(
                        groupId = groupId,
                        academicYear = academicYear,
                        month = month,
                        isPaid = isPaid,
                        limit = pageSize,
                        offset = page * pageSize
                    ).also { 
                    }
                }
                searchQuery.isNotBlank() -> {
                    if (isNumericSearch) {
                        // Search by UID only
                        dao.searchUsersByUid(
                            groupId = groupId,
                            uid = searchQuery.toInt(),
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                        }
                    } else {
                        // Regular text search
                        dao.searchUsersByGroupId(
                            groupId = groupId,
                            query = searchQuery,
                            limit = pageSize,
                            offset = page * pageSize
                        ).also { 
                        }
                    }
                }
                else -> {
                    dao.getUsersByGroupIdPaginated(
                        groupId = groupId,
                        limit = pageSize,
                        offset = page * pageSize
                    ).also { 
                    }
                }
            }

            val nextKey = if (users.size < pageSize) {
                null
            } else {
                page + 1
            }

            LoadResult.Page(
                data = users,
                prevKey = if (page == 0) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
} 