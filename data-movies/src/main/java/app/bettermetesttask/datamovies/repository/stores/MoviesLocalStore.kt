package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.datamovies.database.MoviesDatabase
import app.bettermetesttask.datamovies.database.dao.MoviesDao
import app.bettermetesttask.datamovies.database.entities.LikedMovieEntity
import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MoviesLocalStore @Inject constructor(
    private val database: MoviesDatabase
) {

    private val moviesDao: MoviesDao
        get() = database.getMoviesDao()

    suspend fun getMovies(): List<MovieEntity> {
        return moviesDao.selectMovies()
    }

    suspend fun getMovie(id: Int): MovieEntity? {
        return moviesDao.selectMovieById(id)
    }

    suspend fun likeMovie(id: Int) {
        moviesDao.insertLikedEntry(LikedMovieEntity(id))
    }

    suspend fun dislikeMovie(id: Int) {
        moviesDao.removeLikedEntry(id)
    }

    fun observeLikedMoviesIds(): Flow<List<Int>> {
        return moviesDao.selectLikedEntries().map { movieIdsFlow -> movieIdsFlow.map { it.movieId } }
    }

    suspend fun saveMovies(movies: List<MovieEntity>) {
        moviesDao.insertMovies(movies)
    }
}