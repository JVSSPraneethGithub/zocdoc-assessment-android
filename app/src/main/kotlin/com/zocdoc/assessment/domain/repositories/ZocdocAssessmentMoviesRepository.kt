package com.zocdoc.assessment.domain.repositories

import androidx.paging.PagingData
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import kotlinx.coroutines.flow.Flow

interface ZocdocAssessmentMoviesRepository {
    fun getMoviesList(pageSize: Int, prefetchDistance: Int): Flow<PagingData<MovieEntity>>
    suspend fun getMovieDetails(selectedMovie: MovieEntity): Result<MovieDetailsEntity>
}