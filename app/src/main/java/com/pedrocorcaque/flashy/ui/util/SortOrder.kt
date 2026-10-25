package com.pedrocorcaque.flashy.ui.util

import java.text.Collator
import java.util.Locale

/** Formas de ordenar listas de tópicos e subtópicos. */
enum class SortOrder(val label: String) {
    DEFAULT("Padrão"),
    NAME("A–Z"),
    MOST_CARDS("Mais cards"),
    FEWEST_CARDS("Menos cards")
}

/**
 * Ordena a lista conforme [order]. A ordem alfabética respeita acentos em português
 * (Álgebra vem junto do A, não depois do Z). Empates de quantidade desempatam por nome.
 */
fun <T> List<T>.sortedByOrder(
    order: SortOrder,
    name: (T) -> String,
    cardCount: (T) -> Int
): List<T> {
    val collator = Collator.getInstance(Locale("pt", "BR"))
    val byName = Comparator<T> { a, b -> collator.compare(name(a), name(b)) }
    return when (order) {
        SortOrder.DEFAULT -> this
        SortOrder.NAME -> sortedWith(byName)
        SortOrder.MOST_CARDS -> sortedWith(compareByDescending<T> { cardCount(it) }.thenComparator { a, b -> byName.compare(a, b) })
        SortOrder.FEWEST_CARDS -> sortedWith(compareBy<T> { cardCount(it) }.thenComparator { a, b -> byName.compare(a, b) })
    }
}
