package com.example.passionDaily.constants

object DatabaseConstants {
    // 테이블
    const val TABLE_QUOTES = "quotes"
    const val TABLE_QUOTE_CATEGORIES = "quote_categories"
    const val TABLE_FAVORITES = "favorites"

    // Quote 컬럼
    const val COLUMN_QUOTE_ID = "quote_id"
    const val COLUMN_TEXT = "text"
    const val COLUMN_PERSON = "person"
    const val COLUMN_IMAGE_URL = "image_url"
    const val COLUMN_CATEGORY_ID = "category_id"

    // Category 컬럼
    const val COLUMN_CATEGORY_NAME = "category_name"

    // Favorite 컬럼
    const val COLUMN_ADDED_AT = "added_at"

    // Database 이름
    const val DATABASE_NAME = "passion_daily.db"
}