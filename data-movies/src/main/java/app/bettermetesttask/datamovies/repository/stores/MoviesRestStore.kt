package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class MoviesRestStore @Inject constructor() {

    private val statusCodes = listOf(200, 201, 202, 304, 400)

    suspend fun getMovies(): List<Movie> {
        val statusCode = statusCodes.random()
        if (statusCode >= 400) {
            throw MoviesFetchException("Failed to retrieve movies. Status code: $statusCode")
        }
        delay(Random.nextLong(500, 3_000))
        return MoviesFactory.createMoviesList()
    }
}

class MoviesFetchException(message: String) : Exception(message)