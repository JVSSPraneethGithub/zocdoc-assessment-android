package com.zocdoc.assessment.display.composables

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import com.zocdoc.assessment.R
import com.zocdoc.assessment.data.api.Movie
import com.zocdoc.assessment.data.api.MovieDetails
import com.zocdoc.assessment.display.UiState
import com.zocdoc.assessment.display.ZocdocAssessmentViewModel
import com.zocdoc.assessment.domain.entities.MovieDetailsEntity
import com.zocdoc.assessment.domain.entities.MovieEntity
import com.zocdoc.assessment.ui.theme.Pink80
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalSerializationApi::class, DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
@Ignore("https://issuetracker.google.com/issues/372932107")
class ZocdocAssessmentComposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val json = Json { ignoreUnknownKeys = true }
    private val engine = FakeImageLoaderEngine.Builder()
        .intercept(
            {
                it is String &&
                        listOf("jpg", "png", "svg", "webp", "gif")
                            .any { ext ->
                                it.contains(ext, ignoreCase = true)
                            }
            },
            ColorDrawable(Color(0x00FBE94F).toArgb()).asImage()
        )
        .default(ColorDrawable(Pink80.toArgb()).asImage())
        .build()

    private lateinit var allMovies: List<MovieEntity>
    private lateinit var movieDetails: MovieDetailsEntity
    private lateinit var pagingData: Flow<PagingData<MovieEntity>>
    private lateinit var context: Context
    private val testSnackbarHostState = SnackbarHostState()
    private val clickedMovie = MutableStateFlow<UiState>(UiState.Loading)
    private val testSnapshotStateList = SnapshotStateList<MovieEntity>()
    private val testDisplayList = MutableStateFlow(testSnapshotStateList)

    private val viewModel: ZocdocAssessmentViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        allMovies = this::class.java.classLoader
            ?.getResourceAsStream("all_movies_by_rank.json")?.let {
                json.decodeFromStream<List<Movie>>(it)
            }?.map {
                MovieEntity(
                    movieId = it.movieId,
                    movieName = it.movieName,
                    rank = it.rank,
                    posterUrl = "https://place-hold.it/225x225/FBE94F/00.png" +
                            "?text=Movie-Rank:${it.rank}"
                )
            }!!

        movieDetails = this::class.java.classLoader
            ?.getResourceAsStream("movie_details.json")?.let {
                json.decodeFromStream<List<MovieDetails>>(it)
            }?.map {
                MovieDetailsEntity(
                    movieId = it.movieId,
                    movieName = it.movieName,
                    director = it.director,
                    description = it.description,
                    duration = it.duration,
                    genres = it.genres,
                    cast = it.cast,
                    posterUrl = "https://place-hold.it/2750x1830/FBE94F/00.png" +
                            "?text=Movie-rank:&fontsize=120"
                )
            }?.first()!!

        pagingData = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 5
            ),
            pagingSourceFactory = allMovies.asPagingSourceFactory()
        ).flow

        viewModel.apply {
            coEvery { DEFAULT_PREFETCH_DISTANCE } returns 5
            coEvery { moviesByRank } returns pagingData
            coEvery { displayList } returns testDisplayList
            coEvery { snackbarHostState } returns testSnackbarHostState
            coEvery { updateDisplayList(any<List<MovieEntity>>()) } just Runs
            coEvery { clickedMovieId(any<Int>()) } just Runs
            coEvery { movieDetails } returns clickedMovie
        }

        testSnapshotStateList.addAll(allMovies.slice(0..19))

        composeTestRule.setContent {
            ZocdocAssessmentHomeScreen(
                viewModel = viewModel
            ).also {
                context = LocalContext.current
                SingletonImageLoader.setUnsafe(
                    ImageLoader.Builder(context)
                        .components { add(engine) }
                        .build()
                )
            }
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun test_Compose() {
        // Assert Toolbar
        composeTestRule
            .onNodeWithText(context.getString(R.string.movies_list_title))
            .assertExists()
            .assertIsDisplayed()

        // Assert Rank-1 Movie
        composeTestRule
            .onNodeWithText(
                allMovies[0].movieName
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.movie_poster_image, allMovies[0].movieName)
            )
            .assertExists()
            .assertIsDisplayed()

        // Perform Click on Rank-1 movie
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.movie_poster_image, allMovies[0].movieName)
            )
            .performClick()

        verify(exactly = 1) { viewModel.clickedMovieId(any<Int>()) }

        // Verify Movie-Details Loading
        composeTestRule
            .onAllNodes(
                SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo),
                true
            ).fetchSemanticsNodes()
            .isNotEmpty()

        // Verify Movie-Details Tool-bar title
        composeTestRule
            .onNodeWithText(
                context.getString(R.string.movie_details_title)
            )
            .assertExists()
            .assertIsDisplayed()

        // Verify Movie-Details Navigate-Up icon
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.movie_details_navigate_up)
            )
            .assertExists()
            .assertIsDisplayed()

        clickedMovie.tryEmit(
            UiState.Completed.DetailsSuccess(
                movieDetails.copy(
                    movieId = allMovies[0].movieId,
                    movieName = allMovies[0].movieName,
                    rank = allMovies[0].rank
                )
            )
        )

        // Verify Tool-bar title-change
        composeTestRule
            .onNodeWithText(
                movieDetails.movieName
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(
                    R.string.movie_poster_image,
                    movieDetails.movieName
                )
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                movieDetails.duration
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                movieDetails.director
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                movieDetails.description
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                context.getString(R.string.movie_details_button_text)
            )
            .assertExists()
            .assertIsDisplayed()

        // Navigate Up
        composeTestRule
            .onNodeWithContentDescription(
                context.getString(
                    R.string.movie_poster_image,
                    movieDetails.movieName
                )
            )
            .performClick()

        // Assert Toolbar
        composeTestRule
            .onNodeWithText(context.getString(R.string.movies_list_title))
            .assertExists()
            .assertIsDisplayed()

        // Assert Rank-1 Movie
        composeTestRule
            .onNodeWithText(
                allMovies[0].movieName
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(
                context.getString(R.string.movie_rank, allMovies[0].rank)
            )
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(
                context.getString(R.string.movie_poster_image, allMovies[0].movieName)
            )
            .assertExists()
            .assertIsDisplayed()
    }
}