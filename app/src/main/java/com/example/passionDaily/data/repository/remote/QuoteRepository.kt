//package com.example.passionDaily.data.repository.remote
//
//import com.example.passionDaily.data.remote.model.Quote
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class QuoteRepository(private val db: FirebaseFirestore) {
//    companion object {
//        private const val COLLECTION_QUOTES = "quotes"
//    }
//
//    suspend fun getQuoteById(quoteId: String): Quote? {
//        return db.collection(COLLECTION_QUOTES)
//            .document(quoteId)
//            .get()
//            .await()
//            .toObject(Quote::class.java)
//    }
//
//    suspend fun incrementViews(quoteId: String) {
//        db.collection(COLLECTION_QUOTES)
//            .document(quoteId)
//            .update("views", FieldValue.increment(1))
//    }
//
//    suspend fun incrementShares(quoteId: String) {
//        db.collection(COLLECTION_QUOTES)
//            .document(quoteId)
//            .update("shares", FieldValue.increment(1))
//    }
//}