package com.example.passionDaily.util

import android.util.Log
import com.example.passionDaily.data.remote.model.Quote
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuoteCreator(private val db: FirebaseFirestore) {
    suspend fun addNewQuote(category: QuoteCategory, text: String, person: String) {
        try {
            // 1. 해당 카테고리의 quotes 컬렉션에서 마지막 ID 가져오기
            val lastQuote = db.collection("categories")
                .document(category.toString())
                .collection("quotes")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            Log.d("QuoteCreator", "lastQuote: $lastQuote")

            // 2. 새로운 ID 생성 (패딩된 숫자 사용)
            val lastId = if (!lastQuote.isEmpty) {
                val lastDocument = lastQuote.documents[0]
                val lastIdString = lastDocument.getString("id") ?: "quote_000001"
                val lastNumber = lastIdString.split("_").last().toIntOrNull() ?: 0
                "quote_${String.format("%06d", lastNumber + 1)}"  // 6자리 패딩
            } else {
                "quote_000001"  // 첫 번째 문서
            }

            // 3. 현재 시간 가져오기 및 포맷팅
            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(currentTime))

            // 4. 새로운 Quote 객체 생성
            val newQuote = Quote(
                id = lastId,
                category = category,
                text = text,
                person = person,
                imageUrl = "https://github.com/YoonseolLee/Passion-Daily-Photos/blob/main/images/effort_oceanflag.jpg?raw=true",
                createdAt = formattedDate,
                modifiedAt = formattedDate,
                shareCount = 0
            )

            // 5. Firestore에 저장
            db.collection("categories")
                .document(category.toString())
                .collection("quotes")
                .document(lastId)
                .set(newQuote)
                .await()

            Log.d("QuoteCreator", "새로운 명언 추가 완료: $newQuote")

        } catch (e: Exception) {
            throw Exception("명언 추가 중 오류 발생: ${e.message}")
        }
    }
}
