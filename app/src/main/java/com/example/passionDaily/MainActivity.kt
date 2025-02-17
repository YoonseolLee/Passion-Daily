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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var alarmScheduler: ScheduleDailyQuoteAlarmUseCase

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()
        setTheme(R.style.Theme_Passion_Daily)

        addQuotesToFirestore()

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

    private data class QuoteData(
        val id: String,
        val category: String,
        val person: String,
        val text: String,
        val imageUrl: String,
        val createdAt: String,
        val modifiedAt: String,
        val shareCount: Int
    )

    private fun addQuotesToFirestore() {
        val db = FirebaseFirestore.getInstance()
        // 공통 변수 설정
        val commonImageUrl = "https://github.com/YoonseolLee/Passion-Daily-Photos/blob/main/pictures/wealth.jpg?raw=true"
        val commonCreatedAt = "2025-02-17 14:33"
        val commonModifiedAt = "2025-02-17 14:33"
        val commonCategory = "wealth"
        val commonShareCount = 0

        val quotesData = listOf(
            QuoteData(
                id = "quote_000003",
                category = commonCategory,
                person = "빌 게이츠",
                text = "가난하게 태어난 것은 당신의 잘못이 아니다.\n" +
                        "그러나 가난하게 죽는 것은 당신 잘못이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000004",
                category = commonCategory,
                person = "벤자민 그레이엄",
                text = "기업을 공부하지 않고 투자하는 것은\n" +
                        "포커를 칠 때 카드를 보지 않고 배팅하는 것과 같다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000005",
                category = commonCategory,
                person = "워렌 버핏",
                text = "당신이 잠자는 동안에도 돈이 들어오는 방법을 찾지 못한다면,\n" +
                        "당신은 죽을 때까지 일을 해야 할 것이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000006",
                category = commonCategory,
                person = "오스카 와일드",
                text = "젊었을 때는 인생에서 돈이 가장 중요한 것인 줄 알았다.\n" +
                        "나이가 들고 보니, 그것이 사실이었음을 알았다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000007",
                category = commonCategory,
                person = "프랜시스 베이컨",
                text = "돈은 최고의 종이자 최악의 주인이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000008",
                category = commonCategory,
                person = "프랜시스 베이컨",
                text = "부를 경멸하는 사람이 있다.\n" +
                        "그것은 부자가 될 가망이 없기 때문이다.\n" +
                        "부를 경멸하는 사람의 말을 듣지 말라.\n" +
                        "부를 얻는데 실패한 사람이, 부를 경멸한다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000009",
                category = commonCategory,
                person = "플로이드 메이웨더",
                text = "돈이 전부인 건 아니지만,\n"
                        + "그만한 게 없다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000010",
                category = commonCategory,
                person = "시드니 스미스",
                text = "가난은 결코 부끄러운 것이 아니나,\n" +
                        "단지 지독하게 불편할 뿐이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000011",
                category = commonCategory,
                person = "코코 샤넬",
                text = "세상에는 돈이 있는 사람과 \n" +
                        "부자인 사람이 있다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000012",
                category = commonCategory,
                person = "이디스 워튼",
                text = "돈 생각을 떨쳐내는 유일한 방법은,\n"
                        + "돈을 많이 갖는 것이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000013",
                category = commonCategory,
                person = "로버트 기요사키",
                text = "투자는 위험한 것이 아닙니다.\n" +
                        "투자를 배우지 않는 것이 위험한 것입니다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000014",
                category = commonCategory,
                person = "탈무드 中",
                text = "돈으로 열리지 않는 문은 없다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000015",
                category = commonCategory,
                person = "탈무드",
                text = "재물이 있는 자는 근심거리를 안고 살지만,\n" +
                        "가난한 자는 세상의 모든 걱정을 안고 살아간다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000016",
                category = commonCategory,
                person = "찰리 채플린",
                text = "나는 돈을 벌기 위해 사업을 시작했고,\n" +
                        "거기서 예술이 나왔다.\n" +
                        "사람들이 이 말에 환멸을 느껴도 어쩔 수 없다.\n" +
                        "진실이니까.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000017",
                category = commonCategory,
                person = "빌헬름 뮐러",
                text = "대문으로 가난이 찾아와 문을 두드리면,\n" +
                        "사랑은 창 밖으로 도망가버린다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000018",
                category = commonCategory,
                person = "미국 속담",
                text = "돈으로 행복을 살 순 없다.\n" +
                        "하지만, 난 울더라도 저택에서, \n" +
                        "리무진에서, 전용기에서 울고 싶다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000019",
                category = commonCategory,
                person = "찰리 채플린",
                text = "가난은 결코 매력적인 것도 아니고,\n"
                        + "교훈적인 것도 아니다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000020",
                category = commonCategory,
                person = "스웨덴 속담",
                text = "악마는 부자가 사는 집에도 찾아가지만,\n" +
                        "가난한 사람이 사는 집에는 두 번 찾아간다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000021",
                category = commonCategory,
                person = "집시 속담",
                text = "사람들은 부자가 뱀을 먹으면 질병을 치료하는 것이라고 말하고,\n" +
                        "가난한 자가 뱀을 먹으면 배가 고픈 것이라고 말한다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000022",
                category = commonCategory,
                person = "요한 볼프강 폰 괴테",
                text = "지갑이 가벼우면 마음이 무겁다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000023",
                category = commonCategory,
                person = "마하트마 간디",
                text = "가난이야말로 가장 나쁜 종류의 폭력이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000024",
                category = commonCategory,
                person = "50 Cent",
                text = "부자가 되든가,\n"
                        + "죽을 때까지 노력하든가.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000025",
                category = commonCategory,
                person = "워렌 버핏",
                text = "규칙 1: 절대 돈을 잃지 마라.\n" +
                        "규칙 2: 규칙 1을 절대 잊지 마라.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000026",
                category = commonCategory,
                person = "앤디 워홀",
                text = "돈을 버는 것은 예술이며,\n" +
                        "일하는 것도 예술이고,\n" +
                        "좋은 사업은 최고의 예술입니다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000027",
                category = commonCategory,
                person = "도널드 트럼프",
                text = "나는 하루아침에 부동산으로 성공한 것이 아니다.\n" +
                        "하루에 4시간만 자고,\n"
                        + "일주일에 28시간을 독서한다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000028",
                category = commonCategory,
                person = "워렌 버핏",
                text = "책과 신문 속에 부가 있다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            ),
            QuoteData(
                id = "quote_000029",
                category = commonCategory,
                person = "논어",
                text = "가난하면서 원망하지 않는 것은 어려운 일이고,\n" +
                        "부유하면서 교만하지 않은 것은 쉬운 일이다.",
                imageUrl = commonImageUrl,
                createdAt = commonCreatedAt,
                modifiedAt = commonModifiedAt,
                shareCount = commonShareCount
            )
        )

        // 이미 추가된 데이터인지 확인 후 추가
        lifecycleScope.launch(Dispatchers.IO) {
            quotesData.forEach { quoteData ->
                val docRef = db.collection("categories")
                    .document("wealth")
                    .collection("quotes")
                    .document(quoteData.id)

                docRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (!document.exists()) {
                            docRef.set(quoteData)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                    }
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