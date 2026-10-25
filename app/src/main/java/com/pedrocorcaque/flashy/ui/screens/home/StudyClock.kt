package com.pedrocorcaque.flashy.ui.screens.home

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val SAVE_INTERVAL_MS = 15_000L

/**
 * Relógio que conta o tempo decorrido (iniciar / pausar / zerar) e grava o que já passou
 * via [saveStudyTime] a cada 15 s e ao pausar. O cronômetro e o timer da Home usam o mesmo relógio:
 * o timer só interpreta o tempo decorrido como "quanto falta".
 *
 * [onTick] é chamado a cada 250 ms enquanto o relógio roda, com o tempo decorrido total.
 */
class StudyClock(
    private val scope: CoroutineScope,
    private val saveStudyTime: suspend (Long) -> Unit,
    private val onTick: (elapsedMs: Long) -> Unit = {}
) {
    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var job: Job? = null
    private var accumulatedMs = 0L
    private var startedAtMs = 0L
    private var lastSavedAtMs = 0L

    fun start() {
        if (_isRunning.value) return
        val now = SystemClock.elapsedRealtime()
        startedAtMs = now
        lastSavedAtMs = now
        _isRunning.value = true
        job = scope.launch {
            while (true) {
                val t = SystemClock.elapsedRealtime()
                val elapsed = accumulatedMs + (t - startedAtMs)
                _elapsedMs.value = elapsed
                if (t - lastSavedAtMs >= SAVE_INTERVAL_MS) {
                    saveStudyTime(t - lastSavedAtMs)
                    lastSavedAtMs = t
                }
                onTick(elapsed)
                delay(250)
            }
        }
    }

    fun pause() {
        if (!_isRunning.value) return
        job?.cancel()
        job = null
        val now = SystemClock.elapsedRealtime()
        accumulatedMs += now - startedAtMs
        _elapsedMs.value = accumulatedMs
        _isRunning.value = false
        val unsavedMs = now - lastSavedAtMs
        scope.launch { saveStudyTime(unsavedMs) }
    }

    /** Zera o mostrador. O tempo que já foi gravado no Resumo continua lá. */
    fun reset() {
        pause()
        accumulatedMs = 0L
        _elapsedMs.value = 0L
    }
}
