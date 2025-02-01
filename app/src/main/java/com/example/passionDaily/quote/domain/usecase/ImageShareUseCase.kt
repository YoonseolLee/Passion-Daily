package com.example.passionDaily.quote.domain.usecase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.passionDaily.ui.screens.ShareableQuoteImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ImageShareUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun shareQuoteImage(
        context: Context,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        try {
            val (composeView, rootView) = withContext(Dispatchers.Main) {
                val composeView = createComposeView(context)
                val rootView = getRootView(context)
                rootView.addView(composeView)
                setComposeContent(composeView, imageUrl, quoteText, author)
                Pair(composeView, rootView)
            }

            try {
                withContext(Dispatchers.Main) {
                    renderComposeView(composeView)
                }

                val bitmap = withContext(Dispatchers.Default) {
                    createBitmapFromView(composeView)
                }

                withContext(Dispatchers.IO) {
                    val file = saveBitmapToFile(bitmap)
                    val uri = getShareableFileUri(file)

                    withContext(Dispatchers.Main) {
                        launchShareIntent(context, uri)
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    rootView.removeView(composeView)
                }
            }
        } catch (e: IllegalStateException) {
            withContext(Dispatchers.Main) {
                handleError(context, e, "유효하지 않은 Context입니다")
            }
        } catch (e: SecurityException) {
            withContext(Dispatchers.Main) {
                handleError(context, e, "파일 접근 권한이 없습니다")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                handleError(context, e, "이미지 공유를 실패하였습니다.")
            }
        }
    }

    private fun getRootView(context: Context): ViewGroup {
        val activity = context as? Activity
            ?: throw IllegalStateException("Context is not an Activity")
        return activity.window.decorView as ViewGroup
    }

    private fun createComposeView(context: Context): ComposeView {
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            layoutParams = ViewGroup.LayoutParams(1080, 1920)
            alpha = 0f
        }
    }

    private fun setComposeContent(
        composeView: ComposeView,
        imageUrl: String?,
        quoteText: String,
        author: String
    ) {
        composeView.setContent {
            Box(
                modifier = Modifier
                    .width(1080.dp)
                    .height(1920.dp)
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                ShareableQuoteImage(
                    imageUrl = imageUrl,
                    quoteText = quoteText,
                    author = author
                )
            }
        }
    }

    private suspend fun renderComposeView(composeView: ComposeView) {
        delay(1000)
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, 1080, 1920)
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun shareImage(context: Context, bitmap: Bitmap) {
        val file = saveBitmapToFile(bitmap)
        val uri = getShareableFileUri(file)
        launchShareIntent(context, uri)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val filename = "quote_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file
    }

    private fun getShareableFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun launchShareIntent(context: Context, uri: Uri) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "명언 공유하기"))
    }

    private fun handleError(context: Context, e: Exception, message: String) {
        Log.e("ImageShareManager", "Error sharing image", e)
        Toast.makeText(
            context,
            "이미지 공유 중 오류가 발생했습니다: ${message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
