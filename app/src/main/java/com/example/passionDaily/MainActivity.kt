package com.example.passionDaily

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.passionDaily.manager.alarm.DailyQuoteAlarmScheduler
import com.example.passionDaily.navigation.SetupNavigation
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var alarmScheduler: DailyQuoteAlarmScheduler

    // 권한 요청을 위한 launcher 정의
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 FCM 토큰을 가져옵니다
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "Current FCM token: $token")

                    FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("fcmToken", token)
                            .addOnSuccessListener {
                                Log.d("FCM", "Token updated in Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCM", "Failed to update token", e)
                            }
                    }
                } else {
                    Log.e("FCM", "FCM 토큰 가져오기 실패", task.exception)
                }
            }
    }

    private fun checkNotificationPermission() {
        // Android 13 이상에서만 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 권한이 없는 경우에만 요청
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                // 앱 최초 실행 시 시스템 권한 요청 다이얼로그 표시
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()

        setContent {
            Passion_DailyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    SetupNavigation(navController = navController)
                }
            }
        }
    }
}