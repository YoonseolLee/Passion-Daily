package com.example.passionDaily.data.repository

import com.example.passionDaily.data.dao.TermsConsentDao
import com.example.passionDaily.data.entity.TermsConsentEntity
import javax.inject.Inject

class TermsConsentRepository @Inject constructor(private val termsConsentDao: TermsConsentDao) {

    suspend fun getTermsConsent(userId: Int): TermsConsentEntity? {
        return termsConsentDao.getTermsConsent(userId)
    }

    suspend fun insertTermsConsent(termsConsent: TermsConsentEntity) {
        termsConsentDao.insertTermsConsent(termsConsent)
    }

    suspend fun updateTermsConsent(termsConsent: TermsConsentEntity) {
        termsConsentDao.updateTermsConsent(termsConsent)
    }

    suspend fun deleteTermsConsent(termsConsent: TermsConsentEntity) {
        termsConsentDao.deleteTermsConsent(termsConsent)
    }
}