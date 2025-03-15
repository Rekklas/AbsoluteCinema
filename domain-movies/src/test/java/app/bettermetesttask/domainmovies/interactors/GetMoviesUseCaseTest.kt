package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domaincore.utils.Result.*
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class GetMoviesUseCaseTest {

    @Mock
    private lateinit var repository: MoviesRepository

    private lateinit var observeMoviesUseCase: ObserveMoviesUseCase

    @BeforeEach
    fun setUp() {
        observeMoviesUseCase = ObserveMoviesUseCase(repository)
    }

    @Test
    fun `when getMovies succeeds, it should return movie list`(): Unit = runTest {
        val movies = listOf(Movie(1, "Movie Title", "Description", "poster.jpg"))
        whenever(repository.getMovies()).thenReturn(Success(movies))

        val result = observeMoviesUseCase().first()

        assertTrue(result is Success)
        assertEquals(1, (result as Success).data.size)
        verify(repository).getMovies()
    }

    @Test
    fun `when getMovies fails, it should return error`() = runTest {
        whenever(repository.getMovies()).thenReturn(Error(Exception("Failed to load")))

        val result = observeMoviesUseCase().first()

        assertTrue(result is Error)
        assertEquals("Failed to load", (result as Error).error?.localizedMessage)
        verify(repository).getMovies()
    }
}