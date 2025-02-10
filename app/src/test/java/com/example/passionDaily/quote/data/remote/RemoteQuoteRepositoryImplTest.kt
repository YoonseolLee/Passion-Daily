package com.example.passionDaily.quote.data.remote

import com.example.passionDaily.favorites.data.remote.repository.RemoteFavoriteRepositoryImpl
import com.example.passionDaily.util.MainCoroutineRule
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@ExperimentalCoroutinesApi
class RemoteFavoriteRepositoryImplTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: RemoteFavoriteRepositoryImpl
    private lateinit var currentUser: FirebaseUser
    private lateinit var documentReference: DocumentReference
    private lateinit var collectionReference: CollectionReference
    private lateinit var query: Query
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var documentSnapshot: DocumentSnapshot

    // mockTask 헬퍼 함수
    private inline fun <reified T> mockTask(result: T?, exception: Exception? = null): Task<T> {
        val task: Task<T> = mockk(relaxed = true)
        every { task.isComplete } returns true
        every { task.exception } returns exception
        every { task.isCanceled } returns false
        every { task.result } returns result
        return task
    }

    @Before
    fun setup() {
        firestore = mockk(relaxed = true)
        currentUser = mockk(relaxed = true)
        documentReference = mockk(relaxed = true)
        collectionReference = mockk(relaxed = true)
        query = mockk(relaxed = true)
        querySnapshot = mockk(relaxed = true)
        documentSnapshot = mockk(relaxed = true)
        repository = spyk(RemoteFavoriteRepositoryImpl(firestore))

        every { currentUser.uid } returns "testUserId"
    }

    @Test
    fun `즐겨찾기를 Firestore에 추가한다`() = mainCoroutineRule.runTest {
        // Given
        val documentId = "love_quote_000001"
        val favoriteData = hashMapOf(
            "quoteId" to "123",
            "category" to "love"
        )

        every {
            firestore.collection("favorites")
                .document("testUserId")
                .collection("saved_quotes")
                .document(documentId)
        } returns documentReference

        every { documentReference.set(favoriteData) } returns mockTask(null)

        // When
        repository.addFavoriteToFirestore(currentUser, documentId, favoriteData)

        // Then
        verify {
            firestore.collection("favorites")
            documentReference.set(favoriteData)
        }
    }

    @Test
    fun `Firestore에서 즐겨찾기를 삭제한다`() = mainCoroutineRule.runTest {
        // Given
        val quoteId = "quote1"
        val categoryId = 1
        val documents = listOf(mockk<QueryDocumentSnapshot>(relaxed = true))

        every {
            firestore.collection(any())
                .document(any())
                .collection(any())
                .whereEqualTo(any<String>(), any())
                .whereEqualTo(any<String>(), any())
                .get()
        } returns mockTask(querySnapshot)

        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns documents
        every { documents[0].id } returns "documentId"

        every {
            firestore.collection(any())
                .document(any())
                .collection(any())
                .document(any())
                .delete()
        } returns mockTask(null)

        // When
        repository.deleteFavoriteFromFirestore(currentUser, quoteId, categoryId)

        // Then
        verify {
            firestore.collection(any())
            querySnapshot.documents
        }
    }

    @Test
    fun `마지막 quote 번호를 가져온다`() = mainCoroutineRule.runTest {
        // Given
        val documents = listOf(
            mockk<QueryDocumentSnapshot> {
                every { id } returns "quote_000001"
            },
            mockk<QueryDocumentSnapshot> {
                every { id } returns "quote_000002"
            }
        )

        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns documents

        every {
            firestore.collection("favorites")
                .document("testUserId")
                .collection("saved_quotes")
                .get()
        } returns mockTask(querySnapshot)

        // When
        val result = repository.getLastQuoteNumber(currentUser, "love")

        // Then
        assertThat(result).isEqualTo(2L)
        verify {
            firestore.collection("favorites")
            querySnapshot.documents
        }
    }

    @Test
    fun `즐겨찾기가 없을 때 마지막 quote 번호는 0을 반환한다`() = mainCoroutineRule.runTest {
        // Given
        every { querySnapshot.isEmpty } returns true

        every {
            firestore.collection("favorites")
                .document("testUserId")
                .collection("saved_quotes")
                .get()
        } returns mockTask(querySnapshot)

        // When
        val result = repository.getLastQuoteNumber(currentUser, "love")

        // Then
        assertThat(result).isEqualTo(0L)
        verify {
            firestore.collection("favorites")
        }
    }

    @Test
    fun `Firestore 쿼리 실패시 예외가 발생한다`() = mainCoroutineRule.runTest {
        // Given
        val exception = RuntimeException("Firestore query failed")

        every {
            firestore.collection("favorites")
                .document("testUserId")
                .collection("saved_quotes")
                .get()
        } returns mockTask(null, exception)

        // When & Then
        try {
            repository.getLastQuoteNumber(currentUser, "love")
            error("Expected exception was not thrown")
        } catch (e: Exception) {
            assertThat(e).hasMessageThat().contains("Firestore query failed")
        }

        verify {
            firestore.collection("favorites")
        }
    }
}