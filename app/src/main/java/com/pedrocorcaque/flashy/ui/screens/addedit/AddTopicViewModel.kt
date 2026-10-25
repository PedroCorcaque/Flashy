package com.pedrocorcaque.flashy.ui.screens.addedit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pedrocorcaque.flashy.data.model.Topic
import com.pedrocorcaque.flashy.data.repository.FlashyRepository
import com.pedrocorcaque.flashy.ui.theme.PhysicsColor
import com.pedrocorcaque.flashy.ui.theme.TopicIcons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** [topicId] != null significa que estamos editando um tópico existente. */
class AddTopicViewModel(
    private val repository: FlashyRepository,
    private val topicId: String? = null
) : ViewModel() {
    private val _topicName = MutableStateFlow("")
    val topicName: StateFlow<String> = _topicName

    private val _selectedColor = MutableStateFlow(PhysicsColor)
    val selectedColor: StateFlow<Color> = _selectedColor

    private val _selectedIcon = MutableStateFlow(TopicIcons.DEFAULT_KEY)
    val selectedIcon: StateFlow<String> = _selectedIcon

    private var existingTopic: Topic? = null

    init {
        if (topicId != null) {
            viewModelScope.launch {
                repository.getTopicOnce(topicId)?.let { topic ->
                    existingTopic = topic
                    _topicName.value = topic.name
                    _selectedColor.value = Color(topic.colorArgb)
                    _selectedIcon.value = TopicIcons.resolveKey(topic.iconName, topic.name)
                }
            }
        }
    }

    fun onTopicNameChange(newName: String) {
        _topicName.value = newName
    }

    fun onColorSelect(color: Color) {
        _selectedColor.value = color
    }

    fun onIconSelect(iconKey: String) {
        _selectedIcon.value = iconKey
    }

    fun saveTopic(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existing = existingTopic
            val topic = existing?.copy(
                name = _topicName.value.trim(),
                colorArgb = _selectedColor.value.toArgb(),
                iconName = _selectedIcon.value
            ) ?: Topic(
                id = topicId ?: UUID.randomUUID().toString(),
                name = _topicName.value.trim(),
                colorArgb = _selectedColor.value.toArgb(),
                iconName = _selectedIcon.value
            )
            if (existing != null) repository.updateTopic(topic) else repository.insertTopic(topic)
            onSuccess()
        }
    }
}

class AddTopicViewModelFactory(
    private val repository: FlashyRepository,
    private val topicId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTopicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTopicViewModel(repository, topicId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
