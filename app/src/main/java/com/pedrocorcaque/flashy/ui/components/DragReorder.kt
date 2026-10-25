package com.pedrocorcaque.flashy.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Reordenação por arrastar dentro de uma LazyColumn, sem bibliotecas extras.
 *
 * Como funciona: os itens do trecho reordenável ficam em [items] (uma cópia local) e a LazyColumn
 * usa a chave de cada item ([keyOf]). Ao pressionar e arrastar, o item sob o dedo é trocado de lugar
 * com o vizinho em que o centro dele entra. Quando o dedo solta, [onReordered] recebe a nova ordem.
 */
@Stable
class ReorderState<T : Any>(
    private val listState: LazyListState,
    private val items: SnapshotStateList<T>,
    private val keyOf: (T) -> Any,
    private val onReordered: (List<T>) -> Unit
) {
    /** Chave do item que está sendo arrastado (null = ninguém). */
    var draggedKey by mutableStateOf<Any?>(null)
        private set

    // Posição do centro do item sob o dedo, em coordenadas da área visível da lista.
    private var startCenter by mutableStateOf(0f)
    private var totalDelta by mutableStateOf(0f)
    private var orderAtStart: List<Any> = emptyList()

    private val draggedCenter: Float get() = startCenter + totalDelta

    /** Deslocamento vertical para desenhar o item arrastado exatamente sob o dedo. */
    fun draggedOffsetY(): Float {
        val key = draggedKey ?: return 0f
        val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return 0f
        return draggedCenter - (info.offset + info.size / 2f)
    }

    fun onDragStart(key: Any) {
        val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        draggedKey = key
        startCenter = info.offset + info.size / 2f
        totalDelta = 0f
        orderAtStart = items.map(keyOf)
    }

    fun onDrag(dy: Float) {
        if (draggedKey == null) return
        totalDelta += dy
        swapIfNeeded()
    }

    fun onDragEnd() {
        if (draggedKey == null) return
        draggedKey = null
        totalDelta = 0f
        if (items.map(keyOf) != orderAtStart) {
            onReordered(items.toList())
        }
    }

    private fun swapIfNeeded() {
        val key = draggedKey ?: return
        val fromIndex = items.indexOfFirst { keyOf(it) == key }
        if (fromIndex < 0) return
        val center = draggedCenter
        val itemKeys = items.map(keyOf).toSet()

        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
            info.key != key &&
                info.key in itemKeys &&
                center >= info.offset &&
                center <= info.offset + info.size
        } ?: return

        val toIndex = items.indexOfFirst { keyOf(it) == target.key }
        if (toIndex < 0 || toIndex == fromIndex) return
        items.add(toIndex, items.removeAt(fromIndex))
    }

    /** Rola a lista sozinha quando o item arrastado chega perto da borda de cima ou de baixo. */
    suspend fun autoScroll(edgePx: Float, maxStepPx: Float) {
        while (currentCoroutineContext().isActive && draggedKey != null) {
            val layout = listState.layoutInfo
            val center = draggedCenter
            val top = layout.viewportStartOffset + edgePx
            val bottom = layout.viewportEndOffset - edgePx
            val step = when {
                center < top -> -maxStepPx * ((top - center) / edgePx).coerceIn(0f, 1f)
                center > bottom -> maxStepPx * ((center - bottom) / edgePx).coerceIn(0f, 1f)
                else -> 0f
            }
            if (step != 0f) {
                listState.scrollBy(step)
                swapIfNeeded()
            }
            delay(16)
        }
    }
}

@Composable
fun <T : Any> rememberReorderState(
    listState: LazyListState,
    items: SnapshotStateList<T>,
    keyOf: (T) -> Any,
    onReordered: (List<T>) -> Unit
): ReorderState<T> = remember(listState, items) {
    ReorderState(listState, items, keyOf, onReordered)
}

/**
 * Pressione e segure o item, depois arraste. Use como primeiro modificador do item
 * (antes de qualquer deslocamento visual), para que o gesto não "ande" junto com o item.
 */
fun Modifier.dragToReorder(state: ReorderState<*>, key: Any): Modifier =
    pointerInput(state, key) {
        detectDragGesturesAfterLongPress(
            onDragStart = { state.onDragStart(key) },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount.y)
            },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragEnd() }
        )
    }
