package com.example.passionDaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.passionDaily.navigation.SetupNavigation
import com.example.passionDaily.ui.theme.Passion_DailyTheme
import com.example.passionDaily.util.QuoteCategory
import com.example.passionDaily.util.QuoteCreator
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val quoteCreator = QuoteCreator(db)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        /**
         * 명언 추가 시 사용
         */
//        lifecycleScope.launch {
//        }
    }
}