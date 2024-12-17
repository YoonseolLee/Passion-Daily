package com.example.passionDaily.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TermsConsentDao {
    @Query("SELECT * FROM terms_consent WHERE user_id = :userId")
    suspend fun getTermsConsent(userId: Int): TermsConsentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTermsConsent(termsConsent: TermsConsentEntity)

    @Update
    suspend fun updateTermsConsent(termsConsent: TermsConsentEntity)

    @Delete
    suspend fun deleteTermsConsent(termsConsent: TermsConsentEntity)
}
