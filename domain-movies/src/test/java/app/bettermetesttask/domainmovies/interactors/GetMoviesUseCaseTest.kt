package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domaincore.utils.Result.*
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
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
    fun `when getMovies succeeds, it should return movie list`() = runTest {
        // Given: Repository returns a movie list
        val movies = listOf(
            Movie(1, "Movie Title", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg")
        )

        // Fix: Use `doReturn().when()` for proper stubbing
        doReturn(Success(movies)).`when`(repository).getMovies()

        // When: Fetching movies from use case
        val result = observeMoviesUseCase().first()

        // Then
        assertTrue(result is Success)
        assertEquals(1, (result as Success).data.size)

        // Verify the repository was called once
        verify(repository).getMovies()
    }

    @Test
    fun `when getMovies fails, it should return error`() = runTest {
        // Given: Repository throws an exception
        val exception = Exception("Failed to load")
        doReturn(Error(exception)).`when`(repository).getMovies()

        // When
        val result = observeMoviesUseCase().first()

        // Then
        assertTrue(result is Error)
        assertEquals("Failed to load", (result as Error).error?.localizedMessage)

        // Verify the repository was called once
        verify(repository).getMovies()
    }

    @Test
    fun `when liked movies update, the use case should emit updated movies`() = runTest {
        // Given: Movies repository returns a list of movies
        val movies = listOf(
            Movie(1, "Movie 1", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg"),
            Movie(2, "Movie 2", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg")
        )
        doReturn(Success(movies)).`when`(repository).getMovies()

        // Given: Liked movies flow
        val likedMoviesFlow = MutableStateFlow(listOf(1))
        doReturn(likedMoviesFlow).`when`(repository).observeLikedMovieIds()

        // When: Fetching initial result
        val initialResult = observeMoviesUseCase().first()

        // Then
        assertTrue(initialResult is Success)
        val updatedMovies = (initialResult as Success).data

        assertTrue(updatedMovies.first { it.id == 1 }.liked) // Movie 1 should be liked
        assertFalse(updatedMovies.first { it.id == 2 }.liked) // Movie 2 should not be liked

        // When: Updating liked movies
        likedMoviesFlow.emit(listOf(2))

        // Then: Ensure liked movies state is updated
        val updatedResult = observeMoviesUseCase().first()
        val newUpdatedMovies = (updatedResult as Success).data

        assertFalse(newUpdatedMovies.first { it.id == 1 }.liked) // Movie 1 should now be unliked
        assertTrue(newUpdatedMovies.first { it.id == 2 }.liked)  // Movie 2 should be liked
    }
}