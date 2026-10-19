package com.pedrocorcaque.flashy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic

@Database(
    entities = [Topic::class, Subtopic::class, Flashcard::class],
    version = 2,
    exportSchema = false
)
abstract class FlashyDatabase : RoomDatabase() {
    abstract fun dao(): FlashyDao

    companion object {
        @Volatile
        private var Instance: FlashyDatabase? = null

        // Adiciona subtópicos sem apagar os dados que já existem no aparelho.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `subtopics` (" +
                        "`id` TEXT NOT NULL, `topicId` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `subtopicId` TEXT")
            }
        }

        fun getDatabase(context: Context): FlashyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlashyDatabase::class.java, "flashy_database")
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
