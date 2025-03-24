# Zocdoc Android Mobile app Take-home Assessment

### System Requirements:

* Android Studio Meerkat | 2024.3.1
* Android SDK - Max API 36, Min API 24

### Configuration:

* Gradle: 8.13
* AGP: 8.9
* Kotlin: 2.1.20
* Java-version: 11

### Requirements Spec

Suggested Requirements Specs may be [viewed here](docs/ZocDoc_Take_Home_Challenge.pdf)

### Design Considerations:

* Clean architecture (Jetpack Compose, Jetpack ViewModel, Dagger-Hilt, Domain Use-case) with
  Unidirectional Data-flow.
* SOLID, DRY and KISS, no over-engineering, no over-complications.
* Android Paging-3.
* Unit-tests and UI-test included.

  ````./gradlew testDebugUnitTest````

  ````./gradlew connectedDebugAndroidTest````

**Please note**: UI-Test explicitly ignored due to
issue - https://issuetracker.google.com/issues/372932107

### Acceptance Criteria:

* Movies listed by Rank, fetched using Paging-3 from API
* Progress-Indicator displayed without blocking UI ( API fetching is lightning-fast )
* Place-holder images fetched from mock-CDN.
* Tapping Movie-card in Grid-view displays Movie-Details with following data -
    * Title ( tool-bar )
    * Image
    * Duration
    * Genres
    * Director
    * Cast
    * Plot / Description
* Button to "View Showtimes" - launches custom Chrome-Tab Web-view.

### Extra credits included:

* Landscape-orientation with loss-less state.
* Device Theme ( Color-scheme ) support.
* Unit-tests and UI-test