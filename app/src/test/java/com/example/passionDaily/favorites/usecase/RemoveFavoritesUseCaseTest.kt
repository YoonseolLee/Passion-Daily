package com.example.passionDaily.favorites.usecase

import android.util.Log
import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import com.google.common.truth.Truth.assertThat
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain

@OptIn(ExperimentalCoroutinesApi::class)
class RemoveFavoritesUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var removeFavoritesUseCase: RemoveFavoritesUseCase
    private val localFavoriteRepository: LocalFavoriteRepository = mockk()
    private val localQuoteRepository: LocalQuoteRepository = mockk()
    private val remoteFavoriteRepository: RemoteFavoriteRepository = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()

    @Before
    fun setup() {
        removeFavoritesUseCase = RemoveFavoritesUseCase(
            localFavoriteRepository,
            localQuoteRepository,
            remoteFavoriteRepository
        )
    }

    @Test
    fun `유저가 로그인되어 있으면 getRequiredDataForRemove가 정상적으로 값을 반환한다`() = mainCoroutineRule.runTest {
        // Given
        val categoryId = 1
        val firebaseUser: FirebaseUser = mockk()
        every { firebaseAuth.currentUser } returns firebaseUser

        // When
        val result = removeFavoritesUseCase.getRequiredDataForRemove(firebaseAuth, categoryId)

        // Then
        assertThat(result).isEqualTo(Pair(firebaseUser, categoryId))
    }

    @Test
    fun `유저가 로그인되어 있지 않으면 getRequiredDataForRemove가 null을 반환한다`() = mainCoroutineRule.runTest {
        // Given
        val categoryId = 1
        every { firebaseAuth.currentUser } returns null

        // When
        val result = removeFavoritesUseCase.getRequiredDataForRemove(firebaseAuth, categoryId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `즐겨찾기 삭제 후 즐겨찾기 목록이 남아있으면 인용구 삭제되지 않는다`() = mainCoroutineRule.runTest {
        // Given
        val userId = "user1"
        val quoteId = "quote1"
        val categoryId = 1
        val favoriteEntity = mockk<FavoriteEntity>()
        coEvery { localFavoriteRepository.getFavoritesForQuote(quoteId, categoryId) } returns listOf(favoriteEntity, favoriteEntity) // 남아있는 즐겨찾기 있음
        coEvery { localFavoriteRepository.deleteFavorite(userId, quoteId, categoryId) } just Runs

        // When
        removeFavoritesUseCase.deleteLocalFavorite(userId, quoteId, categoryId)

        // Then
        coVerify { localFavoriteRepository.deleteFavorite(userId, quoteId, categoryId) }
        coVerify(exactly = 0) { localQuoteRepository.deleteQuote(quoteId, categoryId) }
    }

    @Test
    fun `파이어베이스에서 즐겨찾기 삭제가 정상적으로 호출된다`() = mainCoroutineRule.runTest {
        // Given
        val currentUser: FirebaseUser = mockk()
        val quoteId = "quote1"
        val categoryId = 1
        coEvery { remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId) } just Runs

        // When
        removeFavoritesUseCase.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)

        // Then
        coVerify { remoteFavoriteRepository.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId) }
    }
}
