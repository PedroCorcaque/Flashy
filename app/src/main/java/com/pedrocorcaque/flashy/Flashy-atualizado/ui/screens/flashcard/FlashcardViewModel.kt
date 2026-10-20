package com.pedrocorcaque.flashy.ui.screens.flashcard

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Flashcard
import com.pedrocorcaque.flashy.data.model.Subtopic
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Id especial usado para estudar os cards de todos os tópicos de uma vez. */
const val ALL_TOPICS_ID = "all"

/** Estado de uma sessão de estudo. [round] > 1 significa que é uma revisão só dos erros. */
data class StudyUiState(
    val isLoading: Boolean = true,
    val queue: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCards: List<Flashcard> = emptyList(),
    val isFinished: Boolean = false,
    val round: Int = 1
) {
    val total: Int get() = queue.size
}

class FlashcardViewModel(
    private val repository: FlashyRepository,
    private val topicId: String,
    private val subtopicId: String?,
    private val shuffle: Boolean = false
) : ViewModel() {
    val topic: StateFlow<Topic?> = repository.getTopicById(topicId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val subtopic: StateFlow<Subtopic?> = if (subtopicId == null) {
        MutableStateFlow<Subtopic?>(null)
    } else {
        repository.getSubtopicById(subtopicId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /** id do subtópico -> nome, usado para mostrar de qual subtópico é cada card. */
    val subtopicNames: StateFlow<Map<String, String>> = (
        if (topicId == ALL_TOPICS_ID) repository.getAllSubtopics()
        else repository.getSubtopicsForTopic(topicId)
        )
        .map { list -> list.associate { it.id to it.name } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    // Cronômetro da rodada atual: começa ao abrir/reiniciar a rodada e para ao terminar.
    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()
    private var timerJob: Job? = null
    private var timerStartedAtMs = 0L

    private fun startTimer() {
        timerJob?.cancel()
        timerStartedAtMs = SystemClock.elapsedRealtime()
        _elapsedMs.value = 0L
        timerJob = viewModelScope.launch {
            while (true) {
                _elapsedMs.value = SystemClock.elapsedRealtime() - timerStartedAtMs
                delay(250)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _elapsedMs.value = SystemClock.elapsedRealtime() - timerStartedAtMs
    }

    init {
        // Tira uma "foto" dos cards no início: editar cards não embaralha a sessão em andamento.
        viewModelScope.launch {
            val cards = when {
                topicId == ALL_TOPICS_ID -> repository.getAllCards().first()
                subtopicId == null -> repository.getCardsForTopic(topicId).first()
                else -> repository.getCardsForSubtopic(subtopicId).first()
            }
            _uiState.value = StudyUiState(
                isLoading = false,
                queue = if (shuffle) cards.shuffled() else cards
            )
            if (cards.isNotEmpty()) startTimer()
        }
    }

    fun answer(correct: Boolean) {
        _uiState.update { state ->
            val card = state.queue.getOrNull(state.currentIndex)
            if (state.isFinished || card == null) {
                state
            } else {
                val newCorrect = if (correct) state.correctCount + 1 else state.correctCount
                val newWrong = if (correct) state.wrongCards else state.wrongCards + card
                if (state.currentIndex >= state.queue.lastIndex) {
                    state.copy(correctCount = newCorrect, wrongCards = newWrong, isFinished = true)
                } else {
                    state.copy(
                        currentIndex = state.currentIndex + 1,
                        correctCount = newCorrect,
                        wrongCards = newWrong
                    )
                }
            }
        }
        if (_uiState.value.isFinished) stopTimer()
    }

    /** Começa uma nova rodada usando apenas os cards que foram errados na rodada atual. */
    fun retryWrong() {
        val roundBefore = _uiState.value.round
        _uiState.update { state ->
            if (state.wrongCards.isEmpty()) {
                state
            } else {
                StudyUiState(
                    isLoading = false,
                    queue = if (shuffle) state.wrongCards.shuffled() else state.wrongCards,
                    round = state.round + 1
                )
            }
        }
        if (_uiState.value.round != roundBefore) startTimer()
    }
}

class FlashcardViewModelFactory(
    private val repository: FlashyRepository,
    private val topicId: String,
    private val subtopicId: String? = null,
    private val shuffle: Boolean = false
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashcardViewModel(repository, topicId, subtopicId, shuffle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
