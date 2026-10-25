package com.pedrocorcaque.flashy.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import com.pedrocorcaque.flashy.data.util.todayEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DayPoint(val day: Long, val correct: Int, val wrong: Int)

data class SummaryUiState(
    val rangeDays: Int = 7,
    val points: List<DayPoint> = emptyList(),
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val studyMs: Long = 0L,
    val hasAnyData: Boolean = false
) {
    val totalAnswers: Int get() = totalCorrect + totalWrong
    val accuracyPercent: Int
        get() = if (totalAnswers == 0) 0 else Math.round(totalCorrect * 100f / totalAnswers)
}

class SummaryViewModel(repository: FlashyRepository) : ViewModel() {
    private val _rangeDays = MutableStateFlow(7)

    val uiState: StateFlow<SummaryUiState> = combine(
        repository.getDailyStats(),
        repository.getAllStudyTime(),
        _rangeDays
    ) { stats, times, range ->
        val today = todayEpochDay()
        val first = today - (range - 1)
        val byDay = stats.associateBy { it.day }
        // Um ponto por dia do período; dias sem estudo valem zero, para a linha ser contínua.
        val points = (first..today).map { day ->
            val stat = byDay[day]
            DayPoint(day, stat?.correct ?: 0, stat?.wrong ?: 0)
        }
        SummaryUiState(
            rangeDays = range,
            points = points,
            totalCorrect = points.sumOf { it.correct },
            totalWrong = points.sumOf { it.wrong },
            studyMs = times.filter { it.dayEpoch in first..today }.sumOf { it.durationMs },
            hasAnyData = stats.isNotEmpty() || times.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SummaryUiState()
    )

    val rangeDays: StateFlow<Int> = _rangeDays.asStateFlow()

    fun onRangeChange(days: Int) {
        _rangeDays.value = days
    }
}

class SummaryViewModelFactory(private val repository: FlashyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SummaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
