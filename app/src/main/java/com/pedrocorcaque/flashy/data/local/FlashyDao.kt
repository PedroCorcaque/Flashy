package com.pedrocorcaque.flashy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pedrocorcaque.flashy.data.model.DailyStat
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.StudyLog
import com.pedrocorcaque.flashy.data.model.StudyTime
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashyDao {
    @Query("SELECT * FROM topics ORDER BY orderIndex ASC, rowid ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicById(topicId: String): Flow<Topic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    @Query("SELECT * FROM subtopics WHERE topicId = :topicId")
    fun getSubtopicsForTopic(topicId: String): Flow<List<Subtopic>>

    @Query("SELECT * FROM subtopics")
    fun getAllSubtopics(): Flow<List<Subtopic>>

    @Query("SELECT * FROM subtopics WHERE id = :subtopicId")
    fun getSubtopicById(subtopicId: String): Flow<Subtopic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: Subtopic)

    // @Update mantém a posição da linha; REPLACE apagaria e recriaria (o item iria para o fim da lista).
    @Update
    suspend fun updateSubtopic(subtopic: Subtopic)

    @Update
    suspend fun updateTopic(topic: Topic)

    @Query("SELECT MAX(orderIndex) FROM topics")
    suspend fun getMaxTopicOrderIndex(): Int?

    @Query("UPDATE topics SET orderIndex = :orderIndex WHERE id = :topicId")
    suspend fun updateTopicOrderIndex(topicId: String, orderIndex: Int)

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId ORDER BY orderIndex ASC, rowid ASC")
    fun getCardsForTopic(topicId: String): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE subtopicId = :subtopicId ORDER BY orderIndex ASC, rowid ASC")
    fun getCardsForSubtopic(subtopicId: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    // @Update mantém a posição física da linha (REPLACE mandaria o card para o fim da ordem).
    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("SELECT MAX(orderIndex) FROM flashcards WHERE topicId = :topicId")
    suspend fun getMaxOrderIndex(topicId: String): Int?

    @Query("UPDATE flashcards SET orderIndex = :orderIndex WHERE id = :cardId")
    suspend fun updateCardOrderIndex(cardId: String, orderIndex: Int)

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): Flashcard?

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCard(cardId: String)
    
    @Query("UPDATE topics SET cardCount = (SELECT COUNT(*) FROM flashcards WHERE topicId = :topicId) WHERE id = :topicId")
    suspend fun updateTopicCardCount(topicId: String)

    @Query("SELECT * FROM flashcards ORDER BY orderIndex ASC, rowid ASC")
    fun getAllCards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicByIdOnce(topicId: String): Topic?

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: String)

    @Query("DELETE FROM flashcards WHERE topicId = :topicId")
    suspend fun deleteCardsForTopic(topicId: String)

    @Query("DELETE FROM subtopics WHERE topicId = :topicId")
    suspend fun deleteSubtopicsForTopic(topicId: String)

    @Query("DELETE FROM subtopics WHERE id = :subtopicId")
    suspend fun deleteSubtopic(subtopicId: String)

    @Query("UPDATE flashcards SET subtopicId = NULL WHERE subtopicId = :subtopicId")
    suspend fun clearSubtopicFromCards(subtopicId: String)

    // ---- Estatísticas ----
    @Insert
    suspend fun insertStudyLog(log: StudyLog)

    @Query(
        "SELECT dayEpoch AS day, SUM(isCorrect) AS correct, COUNT(*) - SUM(isCorrect) AS wrong " +
            "FROM study_logs GROUP BY dayEpoch ORDER BY dayEpoch"
    )
    fun getDailyStats(): Flow<List<DailyStat>>

    @Query("SELECT durationMs FROM study_time WHERE dayEpoch = :day")
    suspend fun getStudyTimeForDay(day: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyTime(entry: StudyTime)

    @Query("SELECT * FROM study_time")
    fun getAllStudyTime(): Flow<List<StudyTime>>
}
