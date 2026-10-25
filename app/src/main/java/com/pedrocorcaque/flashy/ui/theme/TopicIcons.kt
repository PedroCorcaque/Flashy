package com.pedrocorcaque.flashy.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Catálogo de ícones que dá para escolher em um tópico.
 * O que fica salvo no banco (Topic.iconName) é a chave de texto, não o ícone em si.
 */
object TopicIcons {
    const val DEFAULT_KEY = "book"

    val options: List<Pair<String, ImageVector>> = listOf(
        "book" to Icons.Default.Book,
        "menu_book" to Icons.Default.MenuBook,
        "school" to Icons.Default.School,
        "lightbulb" to Icons.Default.Lightbulb,
        "science" to Icons.Default.Science,
        "biotech" to Icons.Default.Biotech,
        "eco" to Icons.Default.Eco,
        "calculate" to Icons.Default.Calculate,
        "functions" to Icons.Default.Functions,
        "hub" to Icons.Default.Hub,
        "psychology" to Icons.Default.Psychology,
        "account_tree" to Icons.Default.AccountTree,
        "language" to Icons.Default.Language,
        "translate" to Icons.Default.Translate,
        "public" to Icons.Default.Public,
        "history_edu" to Icons.Default.HistoryEdu,
        "gavel" to Icons.Default.Gavel,
        "code" to Icons.Default.Code,
        "computer" to Icons.Default.Computer,
        "palette" to Icons.Default.Palette,
        "music_note" to Icons.Default.MusicNote,
        "fitness_center" to Icons.Default.FitnessCenter,
        "local_hospital" to Icons.Default.LocalHospital,
        "savings" to Icons.Default.Savings
    )

    // Versões antigas do app escolhiam o ícone pelo nome do tópico. Mantemos isso para
    // que os tópicos já existentes continuem com o mesmo ícone.
    private val legacyByName = mapOf(
        "Física" to "hub",
        "Química" to "science",
        "Biologia" to "eco",
        "Matemática" to "calculate"
    )

    /** Chave do ícone de um tópico (a salva, a antiga por nome, ou o padrão). */
    fun resolveKey(iconName: String?, topicName: String): String {
        if (iconName != null && options.any { it.first == iconName }) return iconName
        return legacyByName[topicName] ?: DEFAULT_KEY
    }

    fun vectorFor(key: String): ImageVector =
        options.firstOrNull { it.first == key }?.second ?: Icons.Default.Book

    fun resolve(iconName: String?, topicName: String): ImageVector =
        vectorFor(resolveKey(iconName, topicName))
}
