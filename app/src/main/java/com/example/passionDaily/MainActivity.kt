package com.example.passionDaily

import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.passionDaily.notification.usecase.ScheduleDailyQuoteAlarmUseCase
import com.example.passionDaily.navigation.SetupNavigation
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.example.passionDaily.login.manager.SignupManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var _navController: NavHostController? = null

    override fun onDestroy() {
        super.onDestroy()
        _navController = null
    }

    @Inject
    lateinit var alarmScheduler: ScheduleDailyQuoteAlarmUseCase

    @Inject
    lateinit var signupManager: SignupManager

    private lateinit var navController: NavHostController

    private object PreferenceUtil {
        private const val PREF_NAME = "FCMPrefs"
        private const val KEY_FCM_TOKEN = "fcm_token"

        fun saveFCMToken(context: Context, token: String) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FCM_TOKEN, token)
                .apply()
        }

        fun getFCMToken(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FCM_TOKEN, null)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    PreferenceUtil.saveFCMToken(this, token)
                }
            }
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // 이미 권한이 있는 경우에도 FCM 토큰을 확인
                getFCMToken()
            }
        } else {
            // Android 13 미만에서도 FCM 토큰을 확인
            getFCMToken()
        }
    }

    private suspend fun handleEmailLink(intent: Intent?) {
        val emailLink = intent?.data?.toString() ?: return

        if (Firebase.auth.isSignInWithEmailLink(emailLink)) {
            val savedEmail = signupManager.getSavedEmail()

            if (savedEmail != null) {
                try {
                    signupManager.completeSignIn(savedEmail, emailLink)
                        .onSuccess { user ->
                            withContext(Dispatchers.Main) {
                                navController.navigate("quote") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .onFailure { exception ->
                            Log.e("MainActivity", "Error completing sign in", exception)
                            // 에러 처리
                        }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Exception during sign in completion", e)
                    // 예외 처리
                }
            } else {
                // 저장된 이메일이 없는 경우의 처리
                Log.e("MainActivity", "No saved email found")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        setTheme(R.style.Theme_Passion_Daily)

//        addQuotesToFirestore()

        lifecycleScope.launch {
            handleIntentData(intent)
        }

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

    @CallSuper
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        lifecycleScope.launch {
            handleIntentData(intent)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addOnNewIntentListener { intent ->
            onNewIntent(intent) // 수정된 부분
        }
    }

    private suspend fun handleIntentData(intent: Intent?) {
        intent?.let { nonNullIntent ->
            // 이메일 링크 로그인 처리
            if (Firebase.auth.isSignInWithEmailLink(nonNullIntent.data?.toString() ?: "")) {
                handleEmailLink(nonNullIntent)
                return
            }

            // 기존 URI 데이터 처리
            val data = nonNullIntent.data
            if (data?.host == "quote") {
                val pathSegments = data.pathSegments
                if (pathSegments.size >= 2) {
                    val category = pathSegments[0]
                    val quoteId = pathSegments[1]
                    navigateToQuote(category, quoteId)
                    return
                }
            }

            // 기존 Extra 데이터 처리
            val category = nonNullIntent.getStringExtra("category")
            val quoteId = nonNullIntent.getStringExtra("quoteId")
            if (category != null && quoteId != null) {
                navigateToQuote(category, quoteId)
            }
        }
    }

    private suspend fun navigateToQuote(category: String, quoteId: String) {
        withContext(Dispatchers.Main) {
            while (!::navController.isInitialized) {
                delay(100)
            }

            navController.navigate("quote/$category/$quoteId") {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}