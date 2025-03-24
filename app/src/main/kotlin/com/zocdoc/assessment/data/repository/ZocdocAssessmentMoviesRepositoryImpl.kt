package com.zocdoc.assessment.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.zocdoc.assessment.data.api.ZocdocAssessmentApi
import com.zocdoc.assessment.data.paging.ZocdocAssessmentMoviesListPagingSource
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import com.zocdoc.assessment.domain.repositories.ZocdocAssessmentMoviesRepository

class ZocdocAssessmentMoviesRepositoryImpl(
    private val apiService: ZocdocAssessmentApi
) : ZocdocAssessmentMoviesRepository {

    override fun getMoviesList(pageSize: Int, prefetchDistance: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize,
            prefetchDistance = prefetchDistance
        ),
        pagingSourceFactory = { ZocdocAssessmentMoviesListPagingSource(apiService) }
    ).flow

    override suspend fun getMovieDetails(selectedMovie: MovieEntity) = runCatching {
        apiService.getMovieDetails(
            movieIds = selectedMovie.movieId
        ).first()
    }.map {
        MovieDetailsEntity(
            movieId = it.movieId,
            movieName = it.movieName,
            director = it.director,
            description = it.description,
            duration = it.duration,
            genres = it.genres,
            cast = it.cast,
            posterUrl = "https://place-hold.it/2750x1830/FBE94F/00.png" +
                    "?text=Movie-rank:${selectedMovie.rank}&fontsize=120"
        )
    }
}