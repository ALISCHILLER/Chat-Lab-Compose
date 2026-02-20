package com.msa.chatlab.core.storage.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // messages: add professional columns
            db.execSQL("ALTER TABLE messages ADD COLUMN destination TEXT NOT NULL DEFAULT 'default'")
            db.execSQL("ALTER TABLE messages ADD COLUMN source TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN content_type TEXT NOT NULL DEFAULT 'text/plain'")
            db.execSQL("ALTER TABLE messages ADD COLUMN headers_json TEXT NOT NULL DEFAULT '{}'")
            db.execSQL("ALTER TABLE messages ADD COLUMN status TEXT NOT NULL DEFAULT 'SENT'")
            db.execSQL("ALTER TABLE messages ADD COLUMN last_error TEXT")
            db.execSQL("ALTER TABLE messages ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")

            db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_profile_destination_created ON messages(profile_id, destination, created_at)")

            // runs/events tables
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS runs(
                    id TEXT NOT NULL PRIMARY KEY,
                    profileId TEXT NOT NULL,
                    protocolType TEXT NOT NULL,
                    scenarioPreset TEXT NOT NULL,
                    startedAt INTEGER NOT NULL,
                    endedAt INTEGER NOT NULL,
                    summaryJson TEXT NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS events(
                    id TEXT NOT NULL PRIMARY KEY,
                    runId TEXT NOT NULL,
                    timestamp INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    payloadJson TEXT NOT NULL
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_runId_timestamp ON events(runId, timestamp)")

            // presets table (added in Phase 1)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS presets(
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    durationSec INTEGER NOT NULL,
                    rps INTEGER NOT NULL,
                    payloadBytes INTEGER NOT NULL,
                    pattern TEXT NOT NULL
                )
            """.trimIndent())
        }
    }
}
