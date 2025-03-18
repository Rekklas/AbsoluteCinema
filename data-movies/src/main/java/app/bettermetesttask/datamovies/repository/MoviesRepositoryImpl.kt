package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val restStore: MoviesRestStore,
    private val mapper: MoviesMapper
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> {
        return try {
            Result.Loading
            val remoteMovies = restStore.getMovies()
            localStore.saveMovies(mapper.mapToLocalList(remoteMovies))
            Result.Success(remoteMovies)
        } catch (e: Exception) {
            val localMovies = localStore.getMovies()
            val mappedMovies = mapper.mapFromLocalList(localMovies)
            if (localMovies.isNotEmpty()) {
                Result.Success(mappedMovies)
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun getMovie(id: Int): Result<Movie> {
        return localStore.getMovie(id)?.let {
            Result.Success(mapper.mapFromLocal(it))
        } ?: Result.Error(Exception("There is no movie: $id"))
    }

    override fun observeLikedMovieIds(): Flow<List<Int>> {
        return localStore.observeLikedMoviesIds()
    }

    override suspend fun addMovieToFavorites(movieId: Int) {
        localStore.likeMovie(movieId)
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        localStore.dislikeMovie(movieId)
    }
}