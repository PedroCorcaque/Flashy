package com.pedrocorcaque.flashy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import com.pedrocorcaque.flashy.data.util.todayEpochDay
import com.pedrocorcaque.flashy.ui.util.SortOrder
import com.pedrocorcaque.flashy.ui.util.sortedByOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_COUNTDOWN_MS = 25 * 60_000L

enum class TimerMode(val label: String) {
    STOPWATCH("Cronômetro"),
    COUNTDOWN("Timer")
}

/** Tudo que o cartão de tempo da Home precisa para se desenhar. */
data class TimerUiState(
    val mode: TimerMode = TimerMode.STOPWATCH,
    /** Cronômetro: tempo decorrido. Timer: tempo que falta. */
    val displayMs: Long = 0L,
    val isRunning: Boolean = false,
    val canReset: Boolean = false,
    val countdownTotalMs: Long = DEFAULT_COUNTDOWN_MS
)

class HomeViewModel(private val repository: FlashyRepository) : ViewModel() {
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    /** Tópicos já na ordem escolhida no filtro. */
    val uiState: StateFlow<List<Topic>> = combine(repository.getAllTopics(), _sortOrder) { topics, order ->
        topics.sortedByOrder(order, name = { it.name }, cardCount = { it.cardCount })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    /** Só chamado com a ordenação "Padrão", quando a lista na tela é a própria ordem manual. */
    fun reorderTopics(topicIdsInNewOrder: List<String>) {
        viewModelScope.launch { repository.setTopicOrder(topicIdsInNewOrder) }
    }

    // --- Cronômetro e timer de estudo ---
    // Servem para qualquer estudo (livro, aula, exercícios), não só flashcards. Continuam
    // contando enquanto você usa outras telas, porque a Home fica na pilha de navegação.
    // O tempo é gravado no banco a cada 15 s e ao pausar, e aparece no Resumo.
    private val _mode = MutableStateFlow(TimerMode.STOPWATCH)
    private val _countdownTotalMs = MutableStateFlow(DEFAULT_COUNTDOWN_MS)

    private val _countdownFinished = MutableStateFlow(false)
    /** true quando o timer chega a zero; a tela toca o aviso e mostra o diálogo. */
    val countdownFinished: StateFlow<Boolean> = _countdownFinished.asStateFlow()

    private val stopwatch = StudyClock(viewModelScope, { repository.addStudyTime(it) })
    private val countdown = StudyClock(
        scope = viewModelScope,
        saveStudyTime = { repository.addStudyTime(it) },
        onTick = { elapsed -> onCountdownTick(elapsed) }
    )

    private val anyRunning: StateFlow<Boolean> = combine(stopwatch.isRunning, countdown.isRunning) { a, b -> a || b }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val timerState: StateFlow<TimerUiState> = combine(
        _mode,
        stopwatch.elapsedMs,
        countdown.elapsedMs,
        _countdownTotalMs,
        anyRunning
    ) { mode, stopwatchMs, countdownElapsedMs, totalMs, running ->
        when (mode) {
            TimerMode.STOPWATCH -> TimerUiState(
                mode = mode,
                displayMs = stopwatchMs,
                isRunning = running,
                canReset = stopwatchMs > 0L,
                countdownTotalMs = totalMs
            )
            TimerMode.COUNTDOWN -> TimerUiState(
                mode = mode,
                displayMs = maxOf(0L, totalMs - countdownElapsedMs),
                isRunning = running,
                canReset = countdownElapsedMs > 0L,
                countdownTotalMs = totalMs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimerUiState()
    )

    /** Tempo já gravado hoje (inclui sessões anteriores). */
    val todayStudyMs: StateFlow<Long> = repository.getAllStudyTime()
        .map { list -> list.firstOrNull { it.dayEpoch == todayEpochDay() }?.durationMs ?: 0L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    private fun activeClock(): StudyClock =
        if (_mode.value == TimerMode.COUNTDOWN) countdown else stopwatch

    private fun onCountdownTick(elapsedMs: Long) {
        if (elapsedMs >= _countdownTotalMs.value && !_countdownFinished.value) {
            _countdownFinished.value = true
            countdown.pause()
        }
    }

    /** Só dá para trocar de modo com tudo parado. */
    fun onModeChange(mode: TimerMode) {
        if (anyRunning.value) return
        _mode.value = mode
    }

    fun toggleTimer() {
        val clock = activeClock()
        if (clock.isRunning.value) clock.pause() else clock.start()
    }

    fun resetTimer() {
        activeClock().reset()
    }

    /** Define a duração do timer (1 a 600 minutos). Reinicia a contagem. */
    fun setCountdownMinutes(minutes: Int) {
        if (countdown.isRunning.value) return
        _countdownTotalMs.value = minutes.coerceIn(1, 600) * 60_000L
        countdown.reset()
    }

    fun dismissCountdownFinished() {
        _countdownFinished.value = false
        countdown.reset()
    }
}

class HomeViewModelFactory(private val repository: FlashyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
