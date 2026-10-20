package com.pedrocorcaque.flashy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey val id: String,
    val name: String,
    val cardCount: Int = 0,
    val colorArgb: Int,
    val iconName: String? = null
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
    val subtopicId: String? = null
)
