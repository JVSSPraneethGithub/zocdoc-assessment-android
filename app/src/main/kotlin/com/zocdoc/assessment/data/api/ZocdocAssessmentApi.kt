package com.zocdoc.assessment.data.api

import com.zocdoc.assessment.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface ZocdocAssessmentApi {

    @GET("MoviesByRank")
    suspend fun getMoviesByRank(
        @Query("authToken")
        authToken: String = BuildConfig.AUTH_TOKEN,
        @Query("startRankIndex")
        rank: Int,
        @Query("numMovies")
        pageSize: Int
    ): List<Movie>

    @GET("MovieDetails")
    suspend fun getMovieDetails(
        @Query("authToken")
        authToken: String = BuildConfig.AUTH_TOKEN,
        @Query("movieIds")
        movieIds: Int
    ): List<MovieDetails>
}

@Serializable
data class Movie(
    @SerialName("Rank")
    val rank: Int = Int.MIN_VALUE,
    @SerialName("Id")
    val movieId: Int = Int.MIN_VALUE,
    @SerialName("Name")
    val movieName: String = ""
)

@Serializable
data class MovieDetails(
    @SerialName("Id")
    val movieId: Int = Int.MIN_VALUE,
    @SerialName("Name")
    val movieName: String = "",
    @SerialName("Duration")
    val duration: String = "",
    @SerialName("Description")
    val description: String = "",
    @SerialName("Director")
    val director: String = "",
    @SerialName("Genres")
    val genres: List<String> = arrayListOf(),
    @SerialName("Actors")
    val cast: List<String> = arrayListOf()
)