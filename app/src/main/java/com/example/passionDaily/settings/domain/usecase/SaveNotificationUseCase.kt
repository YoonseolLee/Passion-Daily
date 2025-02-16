package com.example.passionDaily.settings.domain.usecase

import com.example.passionDaily.R
import com.example.passionDaily.user.data.local.repository.LocalUserRepository
import com.example.passionDaily.user.data.remote.repository.RemoteUserRepository
import com.example.passionDaily.resources.StringProvider
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class SaveNotificationUseCase @Inject constructor(
    private val remoteUserRepository: RemoteUserRepository,
    private val localUserRepository: LocalUserRepository,
    private val stringProvider: StringProvider
) {

    suspend fun updateNotificationTimeToFirestore(userId: String, time: LocalTime) {
        val timeString = parseTime(time)
        remoteUserRepository.updateNotificationTimeToFirestore(userId, timeString)
    }

    suspend fun updateNotificationTimeToRoom(userId: String, time: LocalTime) {
        val timeString = parseTime(time)
        localUserRepository.updateNotificationTimeToRoom(userId, timeString)
    }

    private fun parseTime(time: LocalTime): String {
        val timeString = time.format(DateTimeFormatter.ofPattern(stringProvider.getString(R.string.hour_pattern)))
        return timeString
    }
}