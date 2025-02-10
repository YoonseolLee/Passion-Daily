package com.example.passionDaily.settings.domain.usecase

import java.time.LocalTime
import javax.inject.Inject

class ParseTimeUseCase @Inject constructor() {
    fun parseTime(timeStr: String): LocalTime {
        return LocalTime.parse(timeStr)
    }
}