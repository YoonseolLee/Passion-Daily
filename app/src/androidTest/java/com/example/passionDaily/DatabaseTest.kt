package com.example.passionDaily

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.passionDaily.data.database.PassionDailyDatabase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test
    fun checkTables() {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).build()

        // 테이블 조회
        val cursor = db.query(SimpleSQLiteQuery("SELECT name FROM sqlite_master WHERE type='table'"))

        println("=== 데이터베이스 테이블 목록 ===")
        cursor.use {
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                // sqlite_ 로 시작하는 시스템 테이블 제외
                if (!tableName.startsWith("sqlite_")) {
                    println("테이블: $tableName")
                }
            }
        }

        db.close()
    }

    @Test
    fun checkTableColumns() {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassionDailyDatabase::class.java
        ).build()

        // 테이블 조회
        val cursor = db.query(SimpleSQLiteQuery("SELECT name FROM sqlite_master WHERE type='table'"))

        println("=== 데이터베이스 테이블 및 필드 정보 ===")
        cursor.use {
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                // sqlite_ 로 시작하는 시스템 테이블 제외
                if (!tableName.startsWith("sqlite_")) {
                    println("\n테이블: $tableName")

                    // 테이블의 필드 조회
                    val columnCursor = db.query(SimpleSQLiteQuery("PRAGMA table_info($tableName)"))
                    columnCursor.use {
                        println("  필드 목록:")
                        while (columnCursor.moveToNext()) {
                            val columnName = columnCursor.getString(1)
                            val columnType = columnCursor.getString(2)
                            val isNotNull = columnCursor.getInt(3) == 1
                            val defaultValue = columnCursor.getString(4)
                            val isPrimaryKey = columnCursor.getInt(5) == 1

                            println("    - 필드명: $columnName")
                            println("      - 타입: $columnType")
                            println("      - NOT NULL: $isNotNull")
                            println("      - 기본값: ${defaultValue ?: "없음"}")
                            println("      - PRIMARY KEY: $isPrimaryKey")
                        }
                    }
                }
            }
        }

        db.close()
    }
}