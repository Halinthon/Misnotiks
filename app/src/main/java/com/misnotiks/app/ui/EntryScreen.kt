package com.misnotiks.app.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.misnotiks.app.data.DataStore
import com.misnotiks.app.data.Entry
import com.misnotiks.app.data.Field

private const val MAX_FIELDS = 10

private class FieldState(label: String, value: String) {
    var label by mutableStateOf(label)
    var value by mutableStateOf(value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    store: DataStore,
    categoryId: String,
    entryId: String,
    onBack: () -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val category = store.categories.find { it.id == categoryId }
    if (category == null) {
        onBack()
        return
    }

    val isNew = entryId == "new"
    val existing = if (isNew) null else category.entries.find { it.id == entryId }
    if (!isNew && existing == null) {
        onBack()
        return
    }

    var editing by remember(entryId) { mutableStateOf(isNew) }
    var title by remember(entryId) { mutableStateOf(existing?.title ?: "") }
    val fieldStates = remember(entryId) {
        val list = mutableStateListOf<FieldState>()
        val source = existing?.fields ?: listOf(Field("", ""))
        source.forEach { list.add(FieldState(it.label, it.value)) }
        if (list.isEmpty()) list.add(FieldState("", ""))
        list
    }
    var confirmDelete by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }

    fun doSave() {
        val cleanFields = fieldStates
            .filter { it.label.isNotBlank() || it.value.isNotBlank() }
            .map { Field(it.label.trim(), it.value.trim()) }
            .toMutableList()

        if (existing != null) {
            existing.title = title.trim().ifBlank { "Sin titulo" }
            existing.fields = cleanFields
        } else {
            category.entries.add(Entry(store.newId(), title.trim().ifBlank { "Sin titulo" }, cleanFields))
        }
        store.save()
        onChanged()
        if (isNew) onBack() else editing = false
    }

    fun shareField(field: Field) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${field.label}: ${field.value}")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir ${field.label}"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Nueva ficha" else (existing?.title ?: "")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!isNew && !editing) {
                        IconButton(onClick = { editing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        if (store.categories.size > 1) {
                            IconButton(onClick = { showMoveDialog = true }) {
                                Icon(Icons.Filled.DriveFileMove, contentDescription = "Mover a otra categoria")
                            }
                        }
                        IconButton(onClick = {
                            val text = buildString {
                                appendLine(existing?.title ?: "")
                                existing?.fields?.forEach { appendLine("${it.label}: ${it.value}") }
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir ficha"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir ficha")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                    if (editing) {
                        IconButton(onClick = { doSave() }) {
                            Icon(Icons.Filled.Check, contentDescription = "Guardar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (editing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(fieldStates.size) { index ->
                        val fs = fieldStates[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = fs.label,
                                onValueChange = { fs.label = it },
                                label = { Text("Etiqueta ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = fs.value,
                                onValueChange = { fs.value = it },
                                label = { Text("Valor") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (fieldStates.size > 1) fieldStates.removeAt(index)
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Quitar campo")
                            }
                        }
                    }
                }

                if (fieldStates.size < MAX_FIELDS) {
                    TextButton(onClick = { fieldStates.add(FieldState("", "")) }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar campo (${fieldStates.size}/$MAX_FIELDS)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { doSave() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar")
                }
            } else {
                val fields = existing?.fields ?: emptyList()
                if (fields.isEmpty()) {
                    Text("Esta ficha no tiene campos todavia.")
                } else {
                    LazyColumn {
                        items(fields.size) { index ->
                            val field = fields[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(field.label, style = MaterialTheme.typography.labelSmall)
                                        Text(field.value, style = MaterialTheme.typography.bodyLarge)
                                    }
                                    IconButton(onClick = { shareField(field) }) {
                                        Icon(Icons.Filled.Share, contentDescription = "Compartir ${field.label}")
                                    }
                                    IconButton(onClick = {
                                        clipboard.setText(AnnotatedString(field.value))
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar ${field.label}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar ficha") },
            text = { Text("¿Eliminar \"${existing?.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { category.entries.remove(it) }
                    store.save()
                    onChanged()
                    confirmDelete = false
                    onBack()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }

    if (showMoveDialog && existing != null) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Mover a otra categoria") },
            text = {
                Column {
                    store.categories
                        .filter { it.id != category.id }
                        .forEach { target ->
                            Text(
                                target.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                                    .clickable {
                                        category.entries.remove(existing)
                                        target.entries.add(existing)
                                        store.save()
                                        onChanged()
                                        showMoveDialog = false
                                        onBack()
                                    }
                            )
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
