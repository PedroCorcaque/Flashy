package com.pedrocorcaque.flashy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.StudyLog
import com.pedrocorcaque.flashy.data.model.StudyTime
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic

@Database(
    entities = [Topic::class, Subtopic::class, Flashcard::class, StudyLog::class, StudyTime::class],
    version = 5,
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

        // Histórico de respostas (gráfico do Resumo) e tempo do cronômetro geral.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`dayEpoch` INTEGER NOT NULL, `topicId` TEXT NOT NULL, `isCorrect` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `study_time` (" +
                        "`dayEpoch` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`dayEpoch`))"
                )
            }
        }

        // Ordem manual dos cards (arrastar). Cards antigos ficam com 0 e mantêm a ordem de criação.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Ordem manual dos tópicos (arrastar). Tópicos antigos ficam com 0 e mantêm a ordem de criação.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `topics` ADD COLUMN `orderIndex` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): FlashyDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FlashyDatabase::class.java, "flashy_database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
