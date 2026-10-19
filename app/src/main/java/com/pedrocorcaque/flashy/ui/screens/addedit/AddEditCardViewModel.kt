package com.pedrocorcaque.flashy.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AddEditCardViewModel(
    private val repository: FlashyRepository,
    initialTopicId: String,
    private val cardId: String?,
    initialSubtopicId: String? = null
) : ViewModel() {
    private val _front = MutableStateFlow("")
    val front: StateFlow<String> = _front

    private val _back = MutableStateFlow("")
    val back: StateFlow<String> = _back

    private val _selectedTopicId = MutableStateFlow(initialTopicId)
    val selectedTopicId: StateFlow<String> = _selectedTopicId

    private val _selectedSubtopicId = MutableStateFlow(initialSubtopicId)
    val selectedSubtopicId: StateFlow<String?> = _selectedSubtopicId

    val topics: StateFlow<List<Topic>> = repository.getAllTopics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Subtópicos do tópico selecionado; muda junto com o seletor de tópico. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val subtopics: StateFlow<List<Subtopic>> = _selectedTopicId
        .flatMapLatest { repository.getSubtopicsForTopic(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (cardId != null) {
            viewModelScope.launch {
                repository.getCardById(cardId)?.let { card ->
                    _front.value = card.front
                    _back.value = card.back
                    _selectedTopicId.value = card.topicId
                    _selectedSubtopicId.value = card.subtopicId
                }
            }
        }
    }

    fun onFrontChange(newValue: String) {
        _front.value = newValue
    }

    fun onBackChange(newValue: String) {
        _back.value = newValue
    }

    fun onTopicSelected(topicId: String) {
        if (topicId != _selectedTopicId.value) {
            _selectedTopicId.value = topicId
            _selectedSubtopicId.value = null // subtópico pertence ao tópico antigo
        }
    }

    fun onSubtopicSelected(subtopicId: String?) {
        _selectedSubtopicId.value = subtopicId
    }

    fun deleteCard(onSuccess: () -> Unit) {
        val id = cardId ?: return
        viewModelScope.launch {
            repository.deleteCard(id)
            onSuccess()
        }
    }

    fun saveCard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val card = Flashcard(
                id = cardId ?: UUID.randomUUID().toString(),
                topicId = _selectedTopicId.value,
                front = _front.value,
                back = _back.value,
                subtopicId = _selectedSubtopicId.value
            )
            repository.insertFlashcard(card)
            onSuccess()
        }
    }
}

class AddEditCardViewModelFactory(
    private val repository: FlashyRepository,
    private val topicId: String,
    private val cardId: String?,
    private val subtopicId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditCardViewModel(repository, topicId, cardId, subtopicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
