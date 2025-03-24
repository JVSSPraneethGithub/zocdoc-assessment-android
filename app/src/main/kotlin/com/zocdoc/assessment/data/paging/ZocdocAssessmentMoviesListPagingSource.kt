package com.zocdoc.assessment.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zocdoc.assessment.data.api.ZocdocAssessmentApi
import com.zocdoc.assessment.domain.entities.MovieEntity

class ZocdocAssessmentMoviesListPagingSource(
    private val api: ZocdocAssessmentApi
) : PagingSource<Int, MovieEntity>() {
    override fun getRefreshKey(state: PagingState<Int, MovieEntity>): Int? {
        return if (state.pages.isNotEmpty())
            state.pages[state.pages.lastIndex].nextKey else null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieEntity> {
        return try {
            val response = api.getMoviesByRank(
                rank = params.key ?: 1,
                pageSize = params.loadSize
            ).map { movie ->
                MovieEntity(
                    movieId = movie.movieId,
                    movieName = movie.movieName,
                    rank = movie.rank,
                    posterUrl = "https://place-hold.it/225x225/FBE94F/00.png" +
                            "?text=Movie-Rank:${movie.rank}"
                )
            }

            LoadResult.Page(
                data = response,
                prevKey = if (response.isEmpty()) null else response.first().rank,
                nextKey = if (response.isEmpty()) null else response[response.lastIndex].rank + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}