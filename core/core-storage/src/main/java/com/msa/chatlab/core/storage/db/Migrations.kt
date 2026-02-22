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

            // presets table
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

    // ✅ outbox schema fix (lab-friendly)
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS outbox")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS outbox(
                    profile_id TEXT NOT NULL,
                    message_id TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    content_type TEXT NOT NULL,
                    headers_json TEXT NOT NULL,
                    body BLOB NOT NULL,
                    created_at INTEGER NOT NULL,
                    attempt INTEGER NOT NULL DEFAULT 0,
                    last_attempt_at INTEGER,
                    last_error TEXT,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    updated_at INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(profile_id, message_id)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_profile_status_created ON outbox(profile_id, status, created_at)")
        }
    }
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS acks (id TEXT NOT NULL, timestamp INTEGER NOT NULL, PRIMARY KEY(id))")
            db.execSQL("CREATE TABLE IF NOT EXISTS dedup (id TEXT NOT NULL, timestamp INTEGER NOT NULL, PRIMARY KEY(id))")
        }
    }
}
