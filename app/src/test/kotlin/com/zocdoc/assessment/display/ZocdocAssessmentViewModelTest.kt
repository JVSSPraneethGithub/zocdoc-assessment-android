package com.zocdoc.assessment.display

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import com.zocdoc.assessment.data.api.Movie
import com.zocdoc.assessment.data.api.MovieDetails
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMovieDetailsUseCase
import com.zocdoc.assessment.domain.usecase.ZocdocAssessmentMoviesListUsecase
import com.zocdoc.assessment.utils.isSorted
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
class ZocdocAssessmentViewModelTest {
    private val KEY_MOVIES_LIST = "MOVIES_LIST"
    private val KEY_MOVIE_ID = "MOVIE_ID"

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var responseList: List<Movie>
    private lateinit var allMovies: List<MovieEntity>
    private lateinit var responseMovieDetails: MovieDetails
    private lateinit var movieDetails: MovieDetailsEntity

    @MockK
    private lateinit var pagingUseCase: ZocdocAssessmentMoviesListUsecase

    @MockK
    private lateinit var detailsUseCase: ZocdocAssessmentMovieDetailsUseCase

    private lateinit var viewModel: ZocdocAssessmentViewModel
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(StandardTestDispatcher())

        responseList = this::class.java.classLoader
            ?.getResourceAsStream("all_movies_by_rank.json")?.let {
                json.decodeFromStream<List<Movie>>(it)
            }!!

        allMovies = responseList.map {
            MovieEntity(
                movieName = it.movieName,
                rank = it.rank,
                movieId = it.movieId,
                posterUrl = "https://place-hold.it/225x225/FBE94F/00.png" +
                        "?text=Movie-Rank:${it.rank}",
            )
        }

        responseMovieDetails = this::class.java.classLoader
            ?.getResourceAsStream("all_movies_by_rank.json")?.let {
                json.decodeFromStream<List<MovieDetails>>(it)
            }?.first()!!

        movieDetails = MovieDetailsEntity(
            movieId = responseMovieDetails.movieId,
            movieName = responseMovieDetails.movieName,
            duration = responseMovieDetails.duration,
            description = responseMovieDetails.description,
            director = responseMovieDetails.director,
            genres = responseMovieDetails.genres,
            cast = responseMovieDetails.cast,
            posterUrl = "https://place-hold.it/2750x1830/FBE94F/00.png" +
                    "?text=Movie-rank:&fontsize=120"
        )

        coEvery {
            pagingUseCase.invoke(any<Int>(), any<Int>())
        } returns Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5
            ),
            pagingSourceFactory = allMovies.asPagingSourceFactory()
        ).flow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `test viewModel initialization`() = runTest {
        viewModel = ZocdocAssessmentViewModel(
            savedStateHandle = savedStateHandle,
            pagingUseCase = pagingUseCase,
            detailsUseCase = detailsUseCase
        )

        coVerify(exactly = 1) { pagingUseCase.invoke(any<Int>(), any<Int>()) }
        assertEquals(2, savedStateHandle.keys().size)
        assertTrue(savedStateHandle.keys().containsAll(setOf(KEY_MOVIES_LIST, KEY_MOVIE_ID)))
    }

    @Test
    fun `test viewModel displayList`() = runTest {
        var displayListSize: Int? = null
        viewModel = ZocdocAssessmentViewModel(
            savedStateHandle = savedStateHandle,
            pagingUseCase = pagingUseCase,
            detailsUseCase = detailsUseCase
        )
        assertTrue(savedStateHandle.get<List<MovieEntity>>(KEY_MOVIES_LIST)!!.isEmpty())

        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.displayList.collectLatest {
                displayListSize = it.size
            }
        }
        advanceUntilIdle()
        assertNotNull(displayListSize)
        assertEquals(0, displayListSize)
        displayListSize = null

        val snapShot = viewModel.moviesByRank.asSnapshot()
        assertNotNull(snapShot)

        viewModel.updateDisplayList(snapShot)
        advanceUntilIdle()
        assertNotNull(displayListSize)
        assertEquals(snapShot.size, displayListSize)
        assertEquals(
            displayListSize,
            savedStateHandle.get<List<MovieEntity>>(KEY_MOVIES_LIST)!!.size
        )
        assertTrue(
            isSorted(
                savedStateHandle.get<List<MovieEntity>>(KEY_MOVIES_LIST)!!,
                Comparator.comparing { it.rank }
            )
        )
        displayListSize = null

        job.cancelAndJoin()
    }

    @Test
    fun `test viewModel movieDetails`() = runTest {
        val selectedMovie = allMovies[Random.nextInt(allMovies.size)]
        movieDetails = movieDetails.copy(
            movieId = selectedMovie.movieId,
            movieName = selectedMovie.movieName,
            rank = selectedMovie.rank,
            posterUrl = "https://place-hold.it/2750x1830/FBE94F/00.png" +
                    "?text=Movie-rank:${selectedMovie.rank}&fontsize=120"
        )

        coEvery {
            detailsUseCase.invoke(selectedMovie)
        } returns Result.success(movieDetails)

        val uiStateList = mutableListOf<UiState>()
        viewModel = ZocdocAssessmentViewModel(
            savedStateHandle = savedStateHandle,
            pagingUseCase = pagingUseCase,
            detailsUseCase = detailsUseCase
        )
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.movieDetails.collectLatest {
                uiStateList.add(it)
            }
        }

        viewModel.clickedMovieId(selectedMovie.movieId)
        savedStateHandle[KEY_MOVIES_LIST] = allMovies
        advanceUntilIdle()

        assertNotNull(savedStateHandle.get<String>(KEY_MOVIE_ID))
        assertEquals(movieDetails.movieId, savedStateHandle.get<String>(KEY_MOVIE_ID))

        assertTrue(uiStateList.isNotEmpty())
        assertEquals(2, uiStateList.size)
        assertEquals(UiState.Loading, uiStateList[0])
        assertEquals(UiState.Completed.DetailsSuccess(movieDetails), uiStateList[1])
        assertEquals(movieDetails, (uiStateList.last() as UiState.Completed.DetailsSuccess).movie)

        job.cancelAndJoin()
    }
}