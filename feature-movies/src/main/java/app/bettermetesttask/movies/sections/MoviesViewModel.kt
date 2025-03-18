package app.bettermetesttask.movies.sections

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {


    private val _moviesStateFlow = MutableStateFlow<MoviesState>(MoviesState.Initial)
    val moviesStateFlow: StateFlow<MoviesState> = _moviesStateFlow.asStateFlow()

    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie.asStateFlow()

    fun loadMovies() {
        viewModelScope.launch {
            observeMoviesUseCase()
                .onStart { _moviesStateFlow.value = MoviesState.Loading }
                .collect { result ->
                    when (result) {
                        is Result.Success -> _moviesStateFlow.value =
                            MoviesState.Loaded(result.data)

                        is Result.Error -> _moviesStateFlow.value =
                            MoviesState.Error(result.error?.localizedMessage ?: "Unknown error")

                        is Result.Loading -> _moviesStateFlow.value = MoviesState.Loading
                    }
                }
        }
    }

    fun likeMovie(movie: Movie) {
        Log.d("MoviesViewModel", "likeMovie: ${movie.liked}")
        viewModelScope.launch {
            if (movie.liked) {
                dislikeMovieUseCase(movie.id)
            } else {
                likeMovieUseCase(movie.id)
            }

            _moviesStateFlow.update { currentState ->
                if (currentState is MoviesState.Loaded) {
                    val updatedMovies = currentState.movies.map {
                        if (it.id == movie.id) it.copy(liked = !it.liked) else it
                    }
                    MoviesState.Loaded(updatedMovies)
                } else {
                    currentState
                }
            }
        }
    }

    fun openMovieDetails(movie: Movie) {
        _selectedMovie.value = movie
    }

    fun closeMovieDetails() {
        _selectedMovie.value = null
    }
}