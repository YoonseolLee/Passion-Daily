package com.example.passionDaily.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.passionDaily.data.entity.TermsConsentEntity
import com.example.passionDaily.data.entity.UserEntity

data class UserWithTermsConsents(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id",
    )
    val termsConsents: List<TermsConsentEntity>,
)
