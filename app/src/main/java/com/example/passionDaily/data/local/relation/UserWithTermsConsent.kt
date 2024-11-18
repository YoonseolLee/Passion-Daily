package com.example.passionDaily.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.local.entity.TermsConsentEntity
import com.example.passionDaily.data.local.entity.UserEntity

data class UserWithTermsConsent(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )
    val termsConsent: TermsConsentEntity,
)
