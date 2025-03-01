package com.example.passionDaily.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object AnimationSpecs {
    val ContentAnimationSpec = tween<IntOffset>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    val FadeAnimationSpec = tween<Float>(
        durationMillis = 400,
        easing = LinearOutSlowInEasing
    )
}
