package com.pedrocorcaque.flashy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashyDao {
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicById(topicId: String): Flow<Topic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    @Query("SELECT * FROM subtopics WHERE topicId = :topicId ORDER BY name COLLATE NOCASE")
    fun getSubtopicsForTopic(topicId: String): Flow<List<Subtopic>>

    @Query("SELECT * FROM subtopics")
    fun getAllSubtopics(): Flow<List<Subtopic>>

    @Query("SELECT * FROM subtopics WHERE id = :subtopicId")
    fun getSubtopicById(subtopicId: String): Flow<Subtopic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtopic(subtopic: Subtopic)

    @Query("SELECT * FROM flashcards WHERE topicId = :topicId")
    fun getCardsForTopic(topicId: String): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE subtopicId = :subtopicId")
    fun getCardsForSubtopic(subtopicId: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): Flashcard?

    @Query("DELETE FROM flashcards WHERE id = :cardId")
    suspend fun deleteCard(cardId: String)
    
    @Query("UPDATE topics SET cardCount = (SELECT COUNT(*) FROM flashcards WHERE topicId = :topicId) WHERE id = :topicId")
    suspend fun updateTopicCardCount(topicId: String)

    @Query("SELECT * FROM flashcards")
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
}
