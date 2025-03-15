package app.bettermetesttask.datamovies.repository.stores

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.domainmovies.entries.Movie
import javax.inject.Inject

class MoviesMapper @Inject constructor() {
    fun mapToLocal(movie: Movie): MovieEntity {
        return MovieEntity(
            id = movie.id,
            title = movie.title,
            description = movie.description,
            posterPath = movie.posterPath
        )
    }

    fun mapFromLocal(entity: MovieEntity?): Movie {
        return entity?.let {
            Movie(
                id = it.id,
                title = it.title,
                description = it.description,
                posterPath = it.posterPath,
            )
        } ?: throw IllegalArgumentException("MovieEntity is null")
    }

    fun mapToLocalList(movies: List<Movie>?): List<MovieEntity> {
        return movies?.map { mapToLocal(it) } ?: emptyList()
    }

    fun mapFromLocalList(entities: List<MovieEntity>?): List<Movie> {
        return entities?.mapNotNull {
            try {
                mapFromLocal(it)
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
}
