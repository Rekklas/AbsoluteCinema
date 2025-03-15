package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveMoviesUseCase @Inject constructor(
    private val repository: MoviesRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<List<Movie>>> {
        return flow {
            val result = repository.getMovies()
            emit(result)
        }.flatMapLatest { result ->
            when (result) {
                is Result.Success -> {
                    repository.observeLikedMovieIds()
                        .map { likedMovieIds ->
                            val movies = result.data.map { movie ->
                                movie.copy(liked = likedMovieIds.contains(movie.id))
                            }
                            Result.Success(movies)
                        }
                }
                is Result.Error -> flowOf(result)
                is Result.Loading -> flowOf(Result.Loading)
            }
        }
    }
}