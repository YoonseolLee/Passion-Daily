package com.example.passionDaily.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passionDaily.data.entity.TermsConsentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TermsConsentDao {
    @Query("SELECT * FROM terms_consent WHERE user_id = :userId")
    fun getTermsConsentsByUserId(userId: Int): Flow<List<TermsConsentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTermsConsent(termsConsent: TermsConsentEntity)
}
