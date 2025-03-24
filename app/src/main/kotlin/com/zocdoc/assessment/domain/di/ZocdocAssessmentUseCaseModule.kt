package com.zocdoc.assessment.domain.di

import com.zocdoc.assessment.domain.repositories.ZocdocAssessmentMoviesRepository
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMovieDetailsUseCase
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMovieDetailsUseCaseImpl
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMoviesListPagingUseCase
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMoviesListUsecase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ZocdocAssessmentUseCaseModule {

    @Provides
    fun providesMoviesListPagingUseCase(
        moviesListRepository: ZocdocAssessmentMoviesRepository
    ): ZocdocAssessmentMoviesListUsecase =
        ZocdocAssessmentMoviesListPagingUseCase(moviesListRepository)

    @Provides
    fun providesMovieDetailsUseCase(
        repository: ZocdocAssessmentMoviesRepository
    ): ZocdocAssessmentMovieDetailsUseCase =
        ZocdocAssessmentMovieDetailsUseCaseImpl(repository)
}