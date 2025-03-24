package com.zocdoc.assessment.display.composables

import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zocdoc.assessment.R
import com.zocdoc.assessment.display.UiState
import com.zocdoc.assessment.display.ZocdocAssessmentViewModel
import com.zocdoc.assessment.display.ui.theme.Space_16dp
import com.zocdoc.assessment.display.ui.theme.Space_32dp
import com.zocdoc.assessment.display.ui.theme.Space_4dp
import com.zocdoc.assessment.display.ui.theme.Space_8dp

@Composable
fun ZocdocAssessmentMovieDetailsScreen(
    viewModel: ZocdocAssessmentViewModel,
    showLoading: (Boolean) -> Unit,
    updateTopAppBar: (String) -> Unit,
) {
    val movieDetails by viewModel.movieDetails.collectAsStateWithLifecycle()
    updateTopAppBar(stringResource(R.string.movie_details_title))

    when (val state = movieDetails) {
        is UiState.Loading -> {
            showLoading(true)
        }

        is UiState.Completed.DetailsSuccess -> {
            updateTopAppBar(state.movie.movieName)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = state.movie.posterUrl,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = stringResource(
                        R.string.movie_poster_image,
                        state.movie.movieName
                    ),
                    modifier = if (
                        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    ) {
                        Modifier
                            .fillMaxWidth(0.75f)
                            .align(Alignment.CenterHorizontally)
                    } else {
                        Modifier.fillMaxWidth()
                    }.fillMaxHeight(0.45f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space_16dp),
                    verticalArrangement = Arrangement.spacedBy(Space_4dp)
                ) {
                    Row(
                        modifier = Modifier.align(
                            Alignment.Start
                        )
                    ) {
                        Text(
                            text = stringResource(
                                R.string.movie_details_duration
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " ${state.movie.duration}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.movie_details_genres
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.movie.genres.joinToString(separator = ", "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                    Row(
                        modifier = Modifier.align(
                            Alignment.Start
                        )
                    ) {
                        Text(
                            text = stringResource(
                                R.string.movie_details_director
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " ${state.movie.director}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.movie_details_cast
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.movie.cast.joinToString(separator = ", "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = stringResource(
                            R.string.movie_details_plot
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.movie.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(
                    modifier = Modifier.height(Space_32dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    val localContext = LocalContext.current
                    val toolbarColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
                    Button(
                        modifier = Modifier.padding(horizontal = Space_16dp, vertical = Space_8dp),
                        onClick = {
                            CustomTabsIntent.Builder()
                                .setDefaultColorSchemeParams(
                                    CustomTabColorSchemeParams.Builder()
                                        .setToolbarColor(toolbarColor)
                                        .build()
                                ).setUrlBarHidingEnabled(true)
                                .setShowTitle(true)
                                .build()
                                .launchUrl(localContext, "https://zocdoc.com".toUri())
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.movie_details_button_text)
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.height(Space_32dp)
                )
            }
            showLoading(false)
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_movie_details_found),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            showLoading(false)
        }
    }
}