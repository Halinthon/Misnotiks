package com.misnotiks.app.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.misnotiks.app.data.Category
import com.misnotiks.app.data.DataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    store: DataStore,
    refreshKey: Int,
    onOpenCategory: (String) -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            store.exportTo(uri)
            Toast.makeText(context, "Datos exportados", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val ok = store.importFrom(uri)
            if (ok) {
                onChanged()
                Toast.makeText(context, "Datos importados", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "El archivo no es valido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Misnotiks") },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("misnotiks_backup.json") }) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "Exportar datos")
                    }
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Importar datos")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newName = ""
                showAddDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva categoria")
            }
        }
    ) { padding ->
        // se lee refreshKey para forzar recomposicion cuando cambian los datos
        val key = refreshKey
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            items(store.categories, key = { it.id }) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenCategory(cat.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.name)
                            Text("${cat.entries.size} fichas")
                        }
                    }
                    IconButton(onClick = { categoryToDelete = cat }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar categoria")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva categoria") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        store.categories.add(Category(store.newId(), newName.trim()))
                        store.save()
                        onChanged()
                    }
                    showAddDialog = false
                }) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Eliminar categoria") },
            text = { Text("Se eliminara \"${cat.name}\" y todas sus fichas. Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    store.categories.remove(cat)
                    store.save()
                    onChanged()
                    categoryToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}
