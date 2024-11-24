package com.example.passionDaily.data.repository.remote

import com.example.passionDaily.data.remote.model.Author
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthorRepository(private val db: FirebaseFirestore) {
    companion object {
        private const val COLLECTION_AUTHORS = "authors"
    }

    suspend fun getAuthorById(authorId: String): Author? {
        return db.collection(COLLECTION_AUTHORS)
            .document(authorId)
            .get()
            .await()
            .toObject(Author::class.java)
    }

    suspend fun incrementQuoteCount(authorId: String) {
        db.collection(COLLECTION_AUTHORS)
            .document(authorId)
            .update("quoteCount", FieldValue.increment(1))
    }
}