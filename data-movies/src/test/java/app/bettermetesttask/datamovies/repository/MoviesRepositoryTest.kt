package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result.Error
import app.bettermetesttask.domaincore.utils.Result.Success
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
internal class MoviesRepositoryTest {

    @Mock
    private lateinit var localStore: MoviesLocalStore

    @Mock
    private lateinit var restStore: MoviesRestStore

    @Mock
    private lateinit var mapper: MoviesMapper

    private lateinit var repository: MoviesRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MoviesRepositoryImpl(localStore, restStore, mapper)
    }

    @Test
    fun `when getMovies succeeds from remote, it should return movies and save to local storage`() = runTest {
        // Given: Remote API returns movie list
        val remoteMovies = listOf(Movie(1, "Test Movie", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg"))
        val localEntities = listOf(MovieEntity(1, "Test Movie", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg"))

        // Fix: Use lenient() for stubbing
        lenient().doReturn(remoteMovies).`when`(restStore).getMovies()
        lenient().doReturn(localEntities).`when`(mapper).mapToLocalList(remoteMovies)
        lenient().doReturn(remoteMovies).`when`(mapper).mapFromLocalList(localEntities)

        // When
        val result = repository.getMovies()

        // Then
        assert(result is Success)
        assert((result as Success).data.isNotEmpty())

        // Verify local storage is updated
        verify(localStore).saveMovies(localEntities)
    }

    @Test
    fun `when getMovies fails, it should return movies from local storage`() = runTest {
        // Given: Remote API fails, but local storage has data
        val localMovies = listOf(MovieEntity(2, "Local Movie", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg"))
        val mappedMovies = listOf(Movie(2, "Local Movie", "Description", "https://www.themoviedb.org/t/p/w440_and_h660_face/x6FsYvt33846IQnDSFxla9j0RX8.jpg"))

        doThrow(RuntimeException("Network error")).`when`(restStore).getMovies()
        doReturn(localMovies).`when`(localStore).getMovies()
        doReturn(mappedMovies).`when`(mapper).mapFromLocalList(localMovies)

        // When
        val result = repository.getMovies()

        // Then
        assert(result is Success)
        assert((result as Success).data.isNotEmpty()) // Ensure data is retrieved from local storage
    }

    @Test
    fun `when getMovies fails and local storage is empty, it should return an error`() = runTest {
        // Given: Remote API fails, local storage is also empty
        doThrow(RuntimeException("Network error")).`when`(restStore).getMovies()
        doReturn(emptyList<MovieEntity>()).`when`(localStore).getMovies()
        doReturn(emptyList<Movie>()).`when`(mapper).mapFromLocalList(emptyList())

        // When
        val result = repository.getMovies()

        // Then
        assert(result is Error)
        assert((result as Error).error is RuntimeException)
    }
}