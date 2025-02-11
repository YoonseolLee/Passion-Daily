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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var alarmScheduler: ScheduleDailyQuoteAlarmUseCase

    private lateinit var navController: NavHostController

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
                    Log.d("FCM", "Current FCM token: $token")

                    updateFCMToken(token)
                } else {
                    Log.e("FCM", "FCM 토큰 가져오기 실패", task.exception)
                }
            }
    }

    private fun updateFCMToken(token: String) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            // 토큰을 업데이트할 때 document를 생성하거나 업데이트하도록 수정
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(
                    mapOf("fcmToken" to token),
                    SetOptions.merge()  // merge 옵션을 사용하여 다른 필드는 유지
                )
                .addOnSuccessListener {
                    Log.d("FCM", "Token updated in Firestore for user: $uid")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to update token for user: $uid", e)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        getFCMToken()
        setTheme(R.style.Theme_Passion_Daily)

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addOnNewIntentListener { intent ->
            lifecycleScope.launch {
                handleIntentData(intent)
            }
        }
    }

    private suspend fun handleIntentData(intent: Intent?) {
        intent?.let { nonNullIntent ->
            // URI 데이터 확인
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

            // Extra 데이터 확인
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