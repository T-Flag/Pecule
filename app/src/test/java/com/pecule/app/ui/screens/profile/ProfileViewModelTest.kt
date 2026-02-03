package com.pecule.app.ui.screens.profile

import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.local.datastore.UserPreferences
import com.pecule.app.data.repository.IUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeUserPreferencesRepositoryForProfile
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeUserPreferencesRepositoryForProfile()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `firstName is initialized from UserPreferencesRepository`() = runTest(testDispatcher) {
        // Given
        fakeRepository.setFirstName("Marie")

        // When
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.firstName.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals("Marie", viewModel.firstName.value)
        job.cancel()
    }

    @Test
    fun `theme is initialized from UserPreferencesRepository`() = runTest(testDispatcher) {
        // Given
        fakeRepository.setTheme(ThemePreference.DARK)

        // When
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.theme.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(ThemePreference.DARK, viewModel.theme.value)
        job.cancel()
    }

    @Test
    fun `updateFirstName saves name in repository`() = runTest(testDispatcher) {
        // Given
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.firstName.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateFirstName("Pierre")
        advanceUntilIdle()

        // Then
        assertEquals("Pierre", fakeRepository.getCurrentFirstName())
        job.cancel()
    }

    @Test
    fun `updateFirstName with empty name does not save`() = runTest(testDispatcher) {
        // Given
        fakeRepository.setFirstName("Marie")
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.firstName.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateFirstName("")
        advanceUntilIdle()

        // Then - firstName should remain unchanged
        assertEquals("Marie", fakeRepository.getCurrentFirstName())
        job.cancel()
    }

    @Test
    fun `updateTheme DARK saves in repository`() = runTest(testDispatcher) {
        // Given
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.theme.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateTheme(ThemePreference.DARK)
        advanceUntilIdle()

        // Then
        assertEquals(ThemePreference.DARK, fakeRepository.getCurrentTheme())
        job.cancel()
    }

    @Test
    fun `updateTheme LIGHT saves in repository`() = runTest(testDispatcher) {
        // Given
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.theme.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateTheme(ThemePreference.LIGHT)
        advanceUntilIdle()

        // Then
        assertEquals(ThemePreference.LIGHT, fakeRepository.getCurrentTheme())
        job.cancel()
    }

    @Test
    fun `updateTheme AUTO saves in repository`() = runTest(testDispatcher) {
        // Given
        fakeRepository.setTheme(ThemePreference.DARK)
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.theme.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateTheme(ThemePreference.AUTO)
        advanceUntilIdle()

        // Then
        assertEquals(ThemePreference.AUTO, fakeRepository.getCurrentTheme())
        job.cancel()
    }

    @Test
    fun `after updateFirstName, firstName reflects new value`() = runTest(testDispatcher) {
        // Given
        fakeRepository.setFirstName("Marie")
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.firstName.collect {} }
        advanceUntilIdle()
        assertEquals("Marie", viewModel.firstName.value)

        // When
        viewModel.updateFirstName("Sophie")
        advanceUntilIdle()

        // Then
        assertEquals("Sophie", viewModel.firstName.value)
        job.cancel()
    }

    @Test
    fun `after updateTheme, theme reflects new value`() = runTest(testDispatcher) {
        // Given
        viewModel = ProfileViewModel(fakeRepository)
        val job = launch { viewModel.theme.collect {} }
        advanceUntilIdle()
        assertEquals(ThemePreference.AUTO, viewModel.theme.value)

        // When
        viewModel.updateTheme(ThemePreference.LIGHT)
        advanceUntilIdle()

        // Then
        assertEquals(ThemePreference.LIGHT, viewModel.theme.value)
        job.cancel()
    }
}

class FakeUserPreferencesRepositoryForProfile : IUserPreferencesRepository {
    private val _userPreferences = MutableStateFlow(UserPreferences())

    override val userPreferences: Flow<UserPreferences> = _userPreferences

    override val isFirstLaunch: Flow<Boolean> = _userPreferences.map { it.firstName.isEmpty() }

    override suspend fun updateFirstName(firstName: String) {
        _userPreferences.value = _userPreferences.value.copy(firstName = firstName)
    }

    override suspend fun updateTheme(theme: ThemePreference) {
        _userPreferences.value = _userPreferences.value.copy(theme = theme)
    }

    fun setFirstName(name: String) {
        _userPreferences.value = _userPreferences.value.copy(firstName = name)
    }

    fun setTheme(theme: ThemePreference) {
        _userPreferences.value = _userPreferences.value.copy(theme = theme)
    }

    fun getCurrentFirstName(): String = _userPreferences.value.firstName

    fun getCurrentTheme(): ThemePreference = _userPreferences.value.theme
}
