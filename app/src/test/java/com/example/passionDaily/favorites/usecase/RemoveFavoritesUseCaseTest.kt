package com.example.passionDaily.favorites.usecase

import com.example.passionDaily.favorites.data.local.entity.FavoriteEntity
import com.example.passionDaily.favorites.data.local.repository.LocalFavoriteRepository
import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepository
import com.example.passionDaily.quote.data.local.repository.LocalQuoteRepository
import com.example.passionDaily.util.MainCoroutineRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

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
