package com.zocdoc.assessment.display.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.zocdoc.assessment.R
import com.zocdoc.assessment.display.ZocdocAssessmentViewModel
import com.zocdoc.assessment.display.ui.theme.Space_16dp
import com.zocdoc.assessment.display.ui.theme.Space_2dp
import com.zocdoc.assessment.display.ui.theme.Space_4dp
import com.zocdoc.assessment.display.ui.theme.Space_8dp
import com.zocdoc.assessment.domain.entities.MovieEntity

@Composable
fun ZocdocAssessmentMoviesListScreen(
    viewModel: ZocdocAssessmentViewModel,
    onMovieCellClicked: () -> Unit,
    showLoading: (Boolean) -> Unit,
    scrollState: LazyGridState
) {
    val lazyPagingItems = viewModel.moviesByRank.collectAsLazyPagingItems()
    val displayList by viewModel.displayList.collectAsStateWithLifecycle()
    val loadingMore = rememberSaveable { mutableStateOf(false) }
    val pagingError = stringResource(R.string.movies_list_paging_error)
    val endReached = stringResource(R.string.movies_list_end_reached)

    LaunchedEffect(scrollState) {
        snapshotFlow {
            if (scrollState.lastScrolledForward) {
                val lastVisibleIndex =
                    scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = scrollState.layoutInfo.totalItemsCount
                Pair(lastVisibleIndex, totalItems)
            } else {
                Pair(-1, 0)
            }
        }.collect { (lastVisibleIndex, totalItems) ->
            if (lastVisibleIndex in 0..totalItems &&
                totalItems - lastVisibleIndex <= viewModel.DEFAULT_PREFETCH_DISTANCE
            ) {
                if (!loadingMore.value) {
                    loadingMore.value = true
                    lazyPagingItems.refresh()
                }
            } else {
                loadingMore.value = false
            }
        }
    }
    showLoading(true)

    if (displayList.isEmpty() && lazyPagingItems.loadState.hasError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.no_movies_found),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Space_16dp),
                    onClick = { lazyPagingItems.retry() }
                ) {
                    Text(
                        modifier = Modifier.padding(
                            horizontal = Space_16dp,
                            vertical = Space_8dp
                        ),
                        text = stringResource(R.string.retry)
                    )
                }
            }
        }
        showLoading(false)
    } else {
        LazyVerticalGrid(
            state = scrollState,
            columns = if (LocalConfiguration.current.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
            ) GridCells.Fixed(3)
            else GridCells.Fixed(2)
        ) {
            when (lazyPagingItems.loadState.append) {
                is LoadState.NotLoading -> {
                    viewModel.updateDisplayList(lazyPagingItems.itemSnapshotList.items)
                    itemsIndexed(
                        items = displayList
                    ) { _, movie ->
                        ZocodocAssessmentMovieCard(
                            movie = movie,
                            onMovieCellClicked = {
                                viewModel.clickedMovieId(movie.movieId)
                                onMovieCellClicked()
                            }
                        )
                    }
                    if (lazyPagingItems.loadState.append.endOfPaginationReached) {
                        viewModel.showSnackbar(endReached)
                    }
                }

                is LoadState.Error -> {
                    viewModel.showSnackbar(pagingError)
                }

                else -> {}
            }
        }
        showLoading(false)
    }
}

@Composable
private fun ZocodocAssessmentMovieCard(
    movie: MovieEntity,
    onMovieCellClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Space_8dp)
            .clickable(onClick = onMovieCellClicked),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Space_4dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space_4dp)
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = stringResource(
                    R.string.movie_poster_image,
                    movie.movieName
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f, matchHeightConstraintsFirst = true)
                    .background(color = Color.White)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space_4dp, vertical = Space_2dp),
                text = movie.movieName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}