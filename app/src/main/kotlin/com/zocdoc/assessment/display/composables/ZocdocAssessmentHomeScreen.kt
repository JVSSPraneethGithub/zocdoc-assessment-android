package com.zocdoc.assessment.display.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zocdoc.assessment.R
import com.zocdoc.assessment.display.ZocdocAssessmentViewModel
import com.zocdoc.assessment.display.ui.theme.No_Space
import com.zocdoc.assessment.display.ui.theme.Space_16dp
import com.zocdoc.assessment.display.ui.theme.Space_64dp
import com.zocdoc.assessment.display.ui.theme.Space_8dp
import com.zocdoc.assessment.ui.theme.ZocdocAssessmentTheme

sealed class ZocdocAssessmentScreen(
    val route: String
) {
    data object MoviesList : ZocdocAssessmentScreen(route = "MoviesList")
    data object MovieDetails : ZocdocAssessmentScreen(route = "MovieDetails")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZocdocAssessmentHomeScreen(
    viewModel: ZocdocAssessmentViewModel = hiltViewModel()
) {
    ZocdocAssessmentTheme {
        val navController = rememberNavController()

        var topAppBarTitle by remember { mutableStateOf("") }
        var showNavigationUpIcon by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }

        val updateTopAppBarTitle: (String) -> Unit = { topAppBarTitle = it }
        val showLoading: (Boolean) -> Unit = { isLoading = it }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topAppBarTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (showNavigationUpIcon) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.movie_details_navigate_up)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = viewModel.snackbarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomCenter)
                ) {
                    Snackbar(
                        snackbarData = it,
                        modifier = Modifier.padding(horizontal = Space_16dp, vertical = Space_8dp)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                val scrollState: LazyGridState = rememberSaveable(saver = LazyGridState.Saver) {
                    LazyGridState()
                }
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = if (LocalConfiguration.current.orientation
                                == Configuration.ORIENTATION_LANDSCAPE
                            ) Space_64dp else No_Space
                        ),
                    navController = navController,
                    startDestination = ZocdocAssessmentScreen.MoviesList.route
                ) {
                    composable(route = ZocdocAssessmentScreen.MoviesList.route) {
                        ZocdocAssessmentMoviesListScreen(
                            viewModel = viewModel,
                            onMovieCellClicked = {
                                navController.navigate(
                                    ZocdocAssessmentScreen.MovieDetails.route
                                )
                            },
                            showLoading = showLoading,
                            scrollState = scrollState
                        ).also {
                            updateTopAppBarTitle(stringResource(R.string.movies_list_title))
                            showNavigationUpIcon = false
                        }
                    }
                    composable(
                        route = ZocdocAssessmentScreen.MovieDetails.route
                    ) {
                        ZocdocAssessmentMovieDetailsScreen(
                            viewModel = viewModel,
                            showLoading = showLoading,
                            updateTopAppBar = updateTopAppBarTitle
                        ).also {
                            showNavigationUpIcon = true
                        }
                    }
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}