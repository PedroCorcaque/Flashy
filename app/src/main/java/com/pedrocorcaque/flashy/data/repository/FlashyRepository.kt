package com.pedrocorcaque.flashy.data.repository

import androidx.room.withTransaction
import com.pedrocorcaque.flashy.data.local.FlashyDao
import com.pedrocorcaque.flashy.data.local.FlashyDatabase
import com.pedrocorcaque.flashy.data.model.DailyStat
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.StudyLog
import com.pedrocorcaque.flashy.data.model.StudyTime
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.util.todayEpochDay
import kotlinx.coroutines.flow.Flow

class FlashyRepository(
    private val dao: FlashyDao,
    private val database: FlashyDatabase
) {
    fun getAllTopics(): Flow<List<Topic>> = dao.getAllTopics()
    
    fun getTopicById(topicId: String): Flow<Topic?> = dao.getTopicById(topicId)
    
    /** Tópico novo vai para o fim da ordem manual. */
    suspend fun insertTopic(topic: Topic) {
        val next = (dao.getMaxTopicOrderIndex() ?: -1) + 1
        dao.insertTopic(topic.copy(orderIndex = next))
    }

    /** Grava a ordem manual dos tópicos: o primeiro id vira posição 0, e assim por diante. */
    suspend fun setTopicOrder(orderedTopicIds: List<String>) {
        database.withTransaction {
            orderedTopicIds.forEachIndexed { index, id -> dao.updateTopicOrderIndex(id, index) }
        }
    }

    fun getSubtopicsForTopic(topicId: String): Flow<List<Subtopic>> = dao.getSubtopicsForTopic(topicId)

    fun getAllSubtopics(): Flow<List<Subtopic>> = dao.getAllSubtopics()

    fun getSubtopicById(subtopicId: String): Flow<Subtopic?> = dao.getSubtopicById(subtopicId)

    suspend fun insertSubtopic(subtopic: Subtopic) = dao.insertSubtopic(subtopic)

    suspend fun updateSubtopic(subtopic: Subtopic) = dao.updateSubtopic(subtopic)

    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)
    
    fun getCardsForTopic(topicId: String): Flow<List<Flashcard>> = dao.getCardsForTopic(topicId)

    fun getCardsForSubtopic(subtopicId: String): Flow<List<Flashcard>> = dao.getCardsForSubtopic(subtopicId)
    
    private suspend fun nextOrderIndex(topicId: String): Int = (dao.getMaxOrderIndex(topicId) ?: -1) + 1

    suspend fun insertFlashcard(flashcard: Flashcard) {
        val previous = dao.getCardById(flashcard.id)
        when {
            // Card novo: vai para o fim da ordem manual.
            previous == null -> dao.insertFlashcard(flashcard.copy(orderIndex = nextOrderIndex(flashcard.topicId)))
            // Mudou de tópico: vai para o fim do novo tópico.
            previous.topicId != flashcard.topicId ->
                dao.updateFlashcard(flashcard.copy(orderIndex = nextOrderIndex(flashcard.topicId)))
            // Edição normal: mantém o lugar.
            else -> dao.updateFlashcard(flashcard.copy(orderIndex = previous.orderIndex))
        }
        dao.updateTopicCardCount(flashcard.topicId)
        // Se o card mudou de tópico, o contador do tópico antigo também precisa ser atualizado.
        if (previous != null && previous.topicId != flashcard.topicId) {
            dao.updateTopicCardCount(previous.topicId)
        }
    }
    
    suspend fun getCardById(cardId: String): Flashcard? = dao.getCardById(cardId)
    
    suspend fun deleteCard(cardId: String) {
        val card = dao.getCardById(cardId)
        dao.deleteCard(cardId)
        card?.let { dao.updateTopicCardCount(it.topicId) }
    }

    fun getAllCards(): Flow<List<Flashcard>> = dao.getAllCards()

    suspend fun getTopicOnce(topicId: String): Topic? = dao.getTopicByIdOnce(topicId)

    /** Apaga o tópico junto com todos os seus cards e subtópicos. */
    suspend fun deleteTopic(topicId: String) {
        database.withTransaction {
            dao.deleteCardsForTopic(topicId)
            dao.deleteSubtopicsForTopic(topicId)
            dao.deleteTopic(topicId)
        }
    }

    /** Apaga o subtópico; os cards dele continuam existindo, só ficam sem subtópico. */
    suspend fun deleteSubtopic(subtopicId: String) {
        database.withTransaction {
            dao.clearSubtopicFromCards(subtopicId)
            dao.deleteSubtopic(subtopicId)
        }
    }

    /** Grava a ordem manual: o primeiro id da lista vira posição 0, e assim por diante. */
    suspend fun setCardOrder(orderedCardIds: List<String>) {
        database.withTransaction {
            orderedCardIds.forEachIndexed { index, id -> dao.updateCardOrderIndex(id, index) }
        }
    }

    // ---- Estatísticas ----
    suspend fun logAnswer(topicId: String, correct: Boolean) {
        dao.insertStudyLog(StudyLog(dayEpoch = todayEpochDay(), topicId = topicId, isCorrect = correct))
    }

    fun getDailyStats(): Flow<List<DailyStat>> = dao.getDailyStats()

    /** Soma [ms] ao tempo de estudo de hoje (cronômetro geral). */
    suspend fun addStudyTime(ms: Long) {
        if (ms <= 0L) return
        val day = todayEpochDay()
        val current = dao.getStudyTimeForDay(day) ?: 0L
        dao.upsertStudyTime(StudyTime(dayEpoch = day, durationMs = current + ms))
    }

    fun getAllStudyTime(): Flow<List<StudyTime>> = dao.getAllStudyTime()
}
