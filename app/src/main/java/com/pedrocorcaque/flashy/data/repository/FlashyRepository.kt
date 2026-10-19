package com.pedrocorcaque.flashy.data.repository

import com.pedrocorcaque.flashy.data.local.FlashyDao
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import kotlinx.coroutines.flow.Flow

class FlashyRepository(private val dao: FlashyDao) {
    fun getAllTopics(): Flow<List<Topic>> = dao.getAllTopics()
    
    fun getTopicById(topicId: String): Flow<Topic?> = dao.getTopicById(topicId)
    
    suspend fun insertTopic(topic: Topic) = dao.insertTopic(topic)

    fun getSubtopicsForTopic(topicId: String): Flow<List<Subtopic>> = dao.getSubtopicsForTopic(topicId)

    fun getSubtopicById(subtopicId: String): Flow<Subtopic?> = dao.getSubtopicById(subtopicId)

    suspend fun insertSubtopic(subtopic: Subtopic) = dao.insertSubtopic(subtopic)
    
    fun getCardsForTopic(topicId: String): Flow<List<Flashcard>> = dao.getCardsForTopic(topicId)

    fun getCardsForSubtopic(subtopicId: String): Flow<List<Flashcard>> = dao.getCardsForSubtopic(subtopicId)
    
    suspend fun insertFlashcard(flashcard: Flashcard) {
        val previous = dao.getCardById(flashcard.id)
        dao.insertFlashcard(flashcard)
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
        dao.deleteCardsForTopic(topicId)
        dao.deleteSubtopicsForTopic(topicId)
        dao.deleteTopic(topicId)
    }

    /** Apaga o subtópico; os cards dele continuam existindo, só ficam sem subtópico. */
    suspend fun deleteSubtopic(subtopicId: String) {
        dao.clearSubtopicFromCards(subtopicId)
        dao.deleteSubtopic(subtopicId)
    }
}
