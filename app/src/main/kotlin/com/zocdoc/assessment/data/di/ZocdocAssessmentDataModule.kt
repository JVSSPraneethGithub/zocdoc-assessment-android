package com.zocdoc.assessment.data.di

import com.zocdoc.assessment.data.api.ZocdocAssessmentApi
import com.zocdoc.assessment.data.repository.ZocdocAssessmentMoviesRepositoryImpl
import com.zocdoc.assessment.domain.repositories.ZocdocAssessmentMoviesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class ZocdocAssessmentDataModule {

    @Provides
    fun providesMoviesRepository(
        api: ZocdocAssessmentApi
    ): ZocdocAssessmentMoviesRepository =
        ZocdocAssessmentMoviesRepositoryImpl(api)
}