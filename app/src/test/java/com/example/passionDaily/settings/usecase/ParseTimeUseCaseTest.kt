package com.example.passionDaily.settings.usecase

import com.example.passionDaily.settings.domain.usecase.ParseTimeUseCase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.format.DateTimeParseException

@RunWith(JUnit4::class)
class ParseTimeUseCaseTest {
    private lateinit var parseTimeUseCase: ParseTimeUseCase

    @Before
    fun setup() {
        parseTimeUseCase = ParseTimeUseCase()
    }

    @Test
    fun 올바른_시간_형식이_파싱된다() {
        // given
        val timeStr = "09:00"

        // when
        val result = parseTimeUseCase.parseTime(timeStr)

        // then
        assertThat(result.hour).isEqualTo(9)
        assertThat(result.minute).isEqualTo(0)
    }

    @Test(expected = DateTimeParseException::class)
    fun 잘못된_시간_형식은_예외가_발생한다() {
        // when
        parseTimeUseCase.parseTime("invalid_time")
    }
}