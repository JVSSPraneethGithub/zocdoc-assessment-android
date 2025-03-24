package com.zocdoc.assessment.display

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMovieDetailsUseCase
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMoviesListUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UiState {
    data object Loading : UiState
    sealed interface Completed : UiState {
        data class DetailsSuccess(val movie: MovieDetailsEntity) : Completed
        data class Error(val message: String) : Completed
    }
}

@HiltViewModel
class ZocdocAssessmentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pagingUseCase: ZocdocAssessmentMoviesListUsecase,
    private val detailsUseCase: ZocdocAssessmentMovieDetailsUseCase
) : ViewModel() {

    private val KEY_MOVIES_LIST = "MOVIES_LIST"
    private val KEY_MOVIE_ID = "MOVIE_ID"
    private val DEFAULT_PAGE_SIZE = 20

    init {
        savedStateHandle[KEY_MOVIES_LIST] = listOf<MovieEntity>()
    }

    val DEFAULT_PREFETCH_DISTANCE = 5

    val snackbarHostState = SnackbarHostState()

    val moviesByRank = pagingUseCase(DEFAULT_PAGE_SIZE, DEFAULT_PREFETCH_DISTANCE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val displayList: StateFlow<SnapshotStateList<MovieEntity>> = savedStateHandle
        .getStateFlow(KEY_MOVIES_LIST, listOf<MovieEntity>())
        .flatMapLatest { movies ->
            flowOf(movies.toMutableStateList())
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            SnapshotStateList()
        )

    fun updateDisplayList(items: List<MovieEntity>) {
        if (items.isNotEmpty()) {
            val displayList = savedStateHandle.get<List<MovieEntity>>(KEY_MOVIES_LIST)
                ?.toMutableList() ?: mutableListOf()
            items.forEach { movie ->
                if (!displayList.any { it.rank == movie.rank }) {
                    displayList.add(movie)
                }
            }
            displayList.sortedBy { it.rank }
            savedStateHandle[KEY_MOVIES_LIST] = displayList.toList()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val movieDetails: StateFlow<UiState> = savedStateHandle
        .getStateFlow(KEY_MOVIE_ID, -1)
        .flatMapLatest { movieId ->
            flowOf<UiState>(
                UiState.Completed.DetailsSuccess(
                    savedStateHandle.get<MovieDetailsEntity>(movieId.toString())
                        ?: detailsUseCase(
                            savedStateHandle.get<List<MovieEntity>>(KEY_MOVIES_LIST)
                                ?.first {
                                    it.movieId == movieId
                                } ?: throw RuntimeException("Movie not found")
                        ).getOrThrow()
                            .also {
                                savedStateHandle[movieId.toString()] = it
                            }
                )
            )
        }.catch { e ->
            emit(UiState.Completed.Error(e.message ?: "Unknown error"))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            UiState.Loading
        )

    fun clickedMovieId(movieId: Int) {
        savedStateHandle[KEY_MOVIE_ID] = movieId
    }

    fun showSnackbar(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }
}