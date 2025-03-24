package com.zocdoc.assessment.domain.usecase

import androidx.paging.PagingData
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import com.zocdoc.assessment.domain.repositories.ZocdocAssessmentMoviesRepository
import kotlinx.coroutines.flow.Flow

interface ZocdocAssessmentMoviesListUsecase {
    operator fun invoke(pageSize: Int, prefetchDistance: Int): Flow<PagingData<MovieEntity>>
}

interface ZocdocAssessmentMovieDetailsUseCase {
    suspend operator fun invoke(selectedMovie: MovieEntity): Result<MovieDetailsEntity>
}

class ZocdocAssessmentMoviesListPagingUseCase(
    private val moviesListRepository: ZocdocAssessmentMoviesRepository
) : ZocdocAssessmentMoviesListUsecase {
    override operator fun invoke(pageSize: Int, prefetchDistance: Int) =
        moviesListRepository.getMoviesList(pageSize, prefetchDistance)
}

class ZocdocAssessmentMovieDetailsUseCaseImpl(
    private val movieDetailsRepository: ZocdocAssessmentMoviesRepository
) : ZocdocAssessmentMovieDetailsUseCase {
    override suspend operator fun invoke(selectedMovie: MovieEntity) =
        movieDetailsRepository.getMovieDetails(selectedMovie)
}

