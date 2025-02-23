package com.example.passionDaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.passionDaily.navigation.SetupNavigation
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var _navController: NavHostController? = null

    override fun onDestroy() {
        super.onDestroy()
        _navController = null
    }

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTheme(R.style.Theme_Passion_Daily)

//        addQuotesToFirestore()

        setContent {
            Passion_DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    navController = rememberNavController()
                    SetupNavigation(navController = navController)
                }
            }
        }
    }

    /**
     * 명언 추가 시 사용하는 함수
     */

//    private data class QuoteData(
//        val id: String,
//        val category: String,
//        val person: String,
//        val text: String,
//        val imageUrl: String,
//        val createdAt: String,
//        val modifiedAt: String,
//        val shareCount: Int
//    )
//
//    private fun addQuotesToFirestore() {
//        val db = FirebaseFirestore.getInstance()
//        // 공통 변수 설정
//        val commonImageUrl = "https://github.com/YoonseolLee/Passion-Daily-Photos/blob/main/pictures/wealth.jpg?raw=true"
//        val commonCreatedAt = "2025-02-17 14:33"
//        val commonModifiedAt = "2025-02-17 14:33"
//        val commonCategory = "wealth"
//        val commonShareCount = 0
//
//        val quotesData = listOf(
//            QuoteData(
//                id = "quote_000003",
//                category = commonCategory,
//                person = "빌 게이츠",
//                text = "가난하게 태어난 것은 당신의 잘못이 아니다.\n" +
//                        "그러나 가난하게 죽는 것은 당신 잘못이다.",
//                imageUrl = commonImageUrl,
//                createdAt = commonCreatedAt,
//                modifiedAt = commonModifiedAt,
//                shareCount = commonShareCount
//            ),
//        )
//
//        // 이미 추가된 데이터인지 확인 후 추가
//        lifecycleScope.launch(Dispatchers.IO) {
//            quotesData.forEach { quoteData ->
//                val docRef = db.collection("categories")
//                    .document("wealth")
//                    .collection("quotes")
//                    .document(quoteData.id)
//
//                docRef.get().addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val document = task.result
//                        if (!document.exists()) {
//                            docRef.set(quoteData)
//                                .addOnSuccessListener {
//                                }
//                                .addOnFailureListener { e ->
//                                }
//                        }
//                    }
//                }
//            }
//        }
//    }
}