package com.zocdoc.assessment.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieEntity(
    val rank: Int = Int.MIN_VALUE,
    val movieId: Int = Int.MIN_VALUE,
    val movieName: String = "",
    val posterUrl: String = ""
) : Parcelable

@Parcelize
data class MovieDetailsEntity(
    val movieId: Int = Int.MIN_VALUE,
    val rank: Int = Int.MIN_VALUE,
    val movieName: String = "",
    val duration: String = "",
    val description: String = "",
    val director: String = "",
    val genres: List<String> = arrayListOf(),
    val cast: List<String> = arrayListOf(),
    val posterUrl: String = ""
) : Parcelable