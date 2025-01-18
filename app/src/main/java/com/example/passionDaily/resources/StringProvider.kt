package com.example.passionDaily.resources

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 애플리케이션의 문자열 리소스에 대한 접근을 관리하는 프로바이더 클래스
 *
 * 이 클래스는 다음과 같은 책임을 갖는다.
 * - 문자열 리소스의 중앙 집중식 접근 제공
 * - 문자열 형식화 및 매개변수 처리
 *
 * @property context 애플리케이션 컨텍스트
 */
@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 주어진 리소스 ID와 인자들을 사용하여 형식화된 문자열을 반환한다.
     *
     * @param resId 문자열 리소스 ID
     * @param args 형식 문자열에 삽입될 인자들
     * @return 형식화된 문자열
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * 복수형 문자열 리소스를 처리한다.
     *
     * @param resId 복수형 문자열 리소스 ID
     * @param quantity 수량
     * @param args 형식 문자열에 삽입될 인자들
     * @return 형식화된 복수형 문자열
     */
    fun getString(@StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
}