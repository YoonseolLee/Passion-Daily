package com.example.passionDaily.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "terms_consent",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TermsConsentEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "terms_version") val termsVersion: Int,
    @ColumnInfo(name = "consent_date") val consentDate: Long,
    @ColumnInfo(name = "created_date") val createdDate: Long,
)
