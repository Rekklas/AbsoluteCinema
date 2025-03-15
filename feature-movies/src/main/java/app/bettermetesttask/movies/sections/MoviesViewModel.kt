package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {


    private val moviesMutableFlow: MutableStateFlow<MoviesState> = MutableStateFlow(MoviesState.Initial)

    val moviesStateFlow: StateFlow<MoviesState>
        get() = moviesMutableFlow.asStateFlow()

    fun loadMovies() {
        viewModelScope.launch {
            observeMoviesUseCase()
                .onStart { moviesMutableFlow.value = MoviesState.Loading }
                .collect { result ->
                    when (result) {
                        is Result.Success -> moviesMutableFlow.value =
                            MoviesState.Loaded(result.data)

                        is Result.Error -> moviesMutableFlow.value =
                            MoviesState.Error(result.error?.localizedMessage ?: "Unknown error")

                        is Result.Loading -> moviesMutableFlow.value = MoviesState.Loading
                    }
                }
        }
    }

    fun likeMovie(movie: Movie) {
        viewModelScope.launch {
            if (movie.liked) {
                likeMovieUseCase(movie.id)
            } else {
                dislikeMovieUseCase(movie.id)
            }
        }
    }

    fun openMovieDetails(movie: Movie) {
        // TODO: todo todo todo todo
    }
}