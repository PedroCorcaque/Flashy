package com.pedrocorcaque.flashy.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TopicViewModel(
    private val repository: FlashyRepository,
    private val topicId: String
) : ViewModel() {
    val topic: StateFlow<Topic?> = repository.getTopicById(topicId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val subtopics: StateFlow<List<Subtopic>> = repository.getSubtopicsForTopic(topicId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // null = "Todos" (sem filtro de subtópico)
    private val _selectedSubtopicId = MutableStateFlow<String?>(null)
    val selectedSubtopicId: StateFlow<String?> = _selectedSubtopicId.asStateFlow()

    /** Cards do tópico, já filtrados pelo subtópico selecionado. */
    val cards: StateFlow<List<Flashcard>> = combine(
        repository.getCardsForTopic(topicId),
        _selectedSubtopicId
    ) { allCards, selectedId ->
        if (selectedId == null) allCards else allCards.filter { it.subtopicId == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSubtopic(subtopicId: String?) {
        _selectedSubtopicId.value = subtopicId
    }

    fun addSubtopic(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val subtopic = Subtopic(
                id = UUID.randomUUID().toString(),
                topicId = topicId,
                name = trimmed
            )
            repository.insertSubtopic(subtopic)
            _selectedSubtopicId.value = subtopic.id
        }
    }

    fun renameSubtopic(subtopic: Subtopic, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.insertSubtopic(subtopic.copy(name = trimmed))
        }
    }

    /** Os cards do subtópico não são apagados; ficam sem subtópico. */
    fun deleteSubtopic(subtopicId: String) {
        if (_selectedSubtopicId.value == subtopicId) _selectedSubtopicId.value = null
        viewModelScope.launch {
            repository.deleteSubtopic(subtopicId)
        }
    }

    /** Apaga o tópico com todos os cards e subtópicos e depois chama [onDone]. */
    fun deleteTopic(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTopic(topicId)
            onDone()
        }
    }
}

class TopicViewModelFactory(
    private val repository: FlashyRepository,
    private val topicId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopicViewModel(repository, topicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
