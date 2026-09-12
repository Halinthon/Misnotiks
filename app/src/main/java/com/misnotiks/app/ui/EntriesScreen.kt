package com.misnotiks.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.misnotiks.app.data.DataStore
import com.misnotiks.app.data.Entry
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntriesScreen(
    store: DataStore,
    categoryId: String,
    refreshKey: Int,
    onBack: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onChanged: () -> Unit
) {
    val category = store.categories.find { it.id == categoryId }
    if (category == null) {
        onBack()
        return
    }

    var entryToDelete by remember { mutableStateOf<Entry?>(null) }

    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        category.entries.add(to.index, category.entries.removeAt(from.index))
        store.save()
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpenEntry("new") }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva ficha")
            }
        }
    ) { padding ->
        val key = refreshKey
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
                .reorderable(reorderState)
        ) {
            items(category.entries, key = { it.id }) { entry ->
                ReorderableItem(reorderState, key = entry.id) { _ ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.DragHandle,
                            contentDescription = "Arrastrar para reordenar",
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .detectReorderAfterLongPress(reorderState)
                        )
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenEntry(entry.id) }
                        ) {
                            Text(entry.title, modifier = Modifier.padding(16.dp))
                        }
                        IconButton(onClick = { entryToDelete = entry }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar ficha")
                        }
                    }
                }
            }
        }
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Eliminar ficha") },
            text = { Text("¿Eliminar \"${entry.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    category.entries.remove(entry)
                    store.save()
                    onChanged()
                    entryToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
