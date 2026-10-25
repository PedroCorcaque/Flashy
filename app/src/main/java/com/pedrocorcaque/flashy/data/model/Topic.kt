package com.pedrocorcaque.flashy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey val id: String,
    val name: String,
    val cardCount: Int = 0,
    val colorArgb: Int,
    val iconName: String? = null,
    /** Posição na ordem manual (arrastar na tela inicial). Menor aparece primeiro. */
    val orderIndex: Int = 0
)

@Entity(tableName = "subtopics")
data class Subtopic(
    @PrimaryKey val id: String,
    val topicId: String,
    val name: String
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val topicId: String,
    val front: String,
    val back: String,
    val subtopicId: String? = null,
    /** Posição na ordem manual (arrastar). Menor aparece primeiro. */
    val orderIndex: Int = 0
)

/** Uma resposta (acerto ou erro) dada em uma sessão de estudo. Alimenta o gráfico do Resumo. */
@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayEpoch: Long,
    val topicId: String,
    val isCorrect: Boolean
)

/** Tempo total (ms) contabilizado pelo cronômetro geral em um dia. */
@Entity(tableName = "study_time")
data class StudyTime(
    @PrimaryKey val dayEpoch: Long,
    val durationMs: Long
)

/** Resultado agregado de um dia (não é tabela). */
data class DailyStat(
    val day: Long,
    val correct: Int,
    val wrong: Int
)
