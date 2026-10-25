package com.pedrocorcaque.flashy.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import com.pedrocorcaque.flashy.ui.util.SortOrder
import com.pedrocorcaque.flashy.ui.util.sortedByOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/** Subtópico junto com a quantidade de cards dele (para listar e ordenar). */
data class SubtopicUi(val subtopic: Subtopic, val cardCount: Int)

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

    private val allCards: StateFlow<List<Flashcard>> = repository.getCardsForTopic(topicId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _subtopicSort = MutableStateFlow(SortOrder.DEFAULT)
    val subtopicSort: StateFlow<SortOrder> = _subtopicSort.asStateFlow()

    /** Subtópicos com contagem de cards, já na ordem escolhida no filtro. */
    val subtopics: StateFlow<List<SubtopicUi>> = combine(
        repository.getSubtopicsForTopic(topicId),
        allCards,
        _subtopicSort
    ) { subtopics, cards, order ->
        val counts = cards.groupingBy { it.subtopicId }.eachCount()
        subtopics
            .map { SubtopicUi(it, counts[it.id] ?: 0) }
            .sortedByOrder(order, name = { it.subtopic.name }, cardCount = { it.cardCount })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // null = sem filtro (todos os cards do tópico)
    private val _selectedSubtopicId = MutableStateFlow<String?>(null)
    val selectedSubtopicId: StateFlow<String?> = _selectedSubtopicId.asStateFlow()

    /** Cards do tópico, já filtrados pelo subtópico selecionado. */
    val cards: StateFlow<List<Flashcard>> = combine(allCards, _selectedSubtopicId) { all, selectedId ->
        if (selectedId == null) all else all.filter { it.subtopicId == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Recebe os ids dos cards que estão na tela, já na nova ordem (todos do tópico ou só os do
     * subtópico filtrado). Eles ocupam as mesmas "vagas" de antes; os demais cards não se mexem.
     */
    fun reorderCards(displayedIdsInNewOrder: List<String>) {
        val full = allCards.value.toMutableList()
        val displayed = displayedIdsInNewOrder.toSet()
        val slots = full.indices.filter { full[it].id in displayed }
        if (slots.size != displayedIdsInNewOrder.size) return

        val byId = full.associateBy { it.id }
        displayedIdsInNewOrder.forEachIndexed { i, id ->
            byId[id]?.let { full[slots[i]] = it }
        }
        viewModelScope.launch { repository.setCardOrder(full.map { it.id }) }
    }

    fun onSubtopicSortChange(order: SortOrder) {
        _subtopicSort.value = order
    }

    /** Toca num subtópico: seleciona; toca de novo: volta a mostrar todos. */
    fun toggleSubtopic(subtopicId: String) {
        _selectedSubtopicId.value = if (_selectedSubtopicId.value == subtopicId) null else subtopicId
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
            repository.updateSubtopic(subtopic.copy(name = trimmed))
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
