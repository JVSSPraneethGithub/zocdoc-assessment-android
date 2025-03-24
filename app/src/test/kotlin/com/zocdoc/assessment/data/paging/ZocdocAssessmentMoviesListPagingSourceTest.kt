package com.zocdoc.assessment.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.testing.TestPager
import com.zocdoc.assessment.data.api.Movie
import com.zocdoc.assessment.data.api.ZocdocAssessmentApi
import com.zocdoc.assessment.domain.entities.MovieEntity
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalSerializationApi::class)
class ZocdocAssessmentMoviesListPagingSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var responseList: List<Movie>
    private lateinit var allMovies: List<MovieEntity>

    @MockK
    private lateinit var api: ZocdocAssessmentApi

    private lateinit var pagingSource: ZocdocAssessmentMoviesListPagingSource

    private lateinit var pager: TestPager<Int, MovieEntity>

    @Before
    fun setUp() {
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

        coEvery {
            api.getMoviesByRank(any<String>(), 1, 20)
        } returns responseList.slice(0..19)

        coEvery {
            api.getMoviesByRank(any<String>(), 21, 20)
        } returns responseList.slice(20..39)

        coEvery {
            api.getMoviesByRank(any<String>(), 41, 20)
        } returns responseList.slice(40..59)

        coEvery {
            api.getMoviesByRank(any<String>(), 61, 20)
        } returns responseList.slice(60..79)

        coEvery {
            api.getMoviesByRank(any<String>(), 81, 20)
        } returns responseList.slice(80..responseList.lastIndex)

        coEvery {
            api.getMoviesByRank(any<String>(), responseList.size + 1, 20)
        } returns emptyList()

        pagingSource = ZocdocAssessmentMoviesListPagingSource(api)
        pager = TestPager(
            pagingSource = pagingSource,
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun testLoadInitial() = runTest {
        val initialPage = pager.refresh() as Page

        assert(initialPage.data.isNotEmpty())
        assertEquals(20, initialPage.data.size)
        assertEquals(21, initialPage.nextKey)
        assertEquals(1, initialPage.prevKey)
        assertEquals(allMovies.slice(0..19), initialPage.data)
    }

    @Test
    fun testLoadThreePages() = runTest {
        val page = with(pager) {
            refresh()
            append()
            append()
        } as Page

        assert(page.data.isNotEmpty())
        assertEquals(20, page.data.size)
        assertEquals(61, page.nextKey)
        assertEquals(41, page.prevKey)
        assertEquals(allMovies.slice(40..59), page.data)
    }

    @Test
    fun testLoadLastPage() = runTest {
        val lastPage = with(pager) {
            refresh()
            append()
            append()
            append()
            append()
        } as Page<Int, MovieEntity>

        assert(lastPage.data.isNotEmpty())
        assertEquals(18, lastPage.data.size)
        assertEquals(allMovies.size + 1, lastPage.nextKey)
        assertEquals(81, lastPage.prevKey)
        assertEquals(allMovies.slice(80..allMovies.lastIndex), lastPage.data)
    }

    @Test
    fun testLoadBeyondLimit() = runTest {
        val lastPage = with(pager) {
            refresh()
            append()
            append()
            append()
            append()
            append()
        } as Page<Int, MovieEntity>

        assert(lastPage.data.isEmpty())
        assertNull(lastPage.nextKey)
        assertNull(lastPage.prevKey)
    }
}