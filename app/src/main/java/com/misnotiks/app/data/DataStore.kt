package com.misnotiks.app.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Field(
    var label: String,
    var value: String
)

data class Entry(
    var id: String,
    var title: String,
    var fields: MutableList<Field> = mutableStateListOf()
)

data class Category(
    var id: String,
    var name: String,
    var entries: MutableList<Entry> = mutableStateListOf()
)

/**
 * Almacena todos los datos de la app en un unico archivo JSON,
 * guardado en el almacenamiento privado de la app (sin cifrar).
 */
class DataStore(private val context: Context) {

    private val file = File(context.filesDir, "misnotiks_data.json")

    var categories: MutableList<Category> = mutableStateListOf()
        private set

    init {
        load()
    }

    private fun load() {
        categories = mutableStateListOf()
        if (!file.exists()) return
        val text = file.readText()
        if (text.isBlank()) return
        runCatching {
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                categories.add(parseCategory(arr.getJSONObject(i)))
            }
        }
    }

    private fun parseCategory(c: JSONObject): Category {
        val entries = mutableStateListOf<Entry>()
        val entriesArr = c.optJSONArray("entries") ?: JSONArray()
        for (j in 0 until entriesArr.length()) {
            entries.add(parseEntry(entriesArr.getJSONObject(j)))
        }
        return Category(
            id = c.optString("id", newId()),
            name = c.optString("name", ""),
            entries = entries
        )
    }

    private fun parseEntry(e: JSONObject): Entry {
        val fields = mutableStateListOf<Field>()
        val fieldsArr = e.optJSONArray("fields") ?: JSONArray()
        for (k in 0 until fieldsArr.length()) {
            val f = fieldsArr.getJSONObject(k)
            fields.add(Field(f.optString("label"), f.optString("value")))
        }
        return Entry(
            id = e.optString("id", newId()),
            title = e.optString("title", ""),
            fields = fields
        )
    }

    fun save() {
        val arr = JSONArray()
        for (c in categories) {
            val co = JSONObject()
            co.put("id", c.id)
            co.put("name", c.name)
            val entriesArr = JSONArray()
            for (e in c.entries) {
                val eo = JSONObject()
                eo.put("id", e.id)
                eo.put("title", e.title)
                val fieldsArr = JSONArray()
                for (f in e.fields) {
                    val fo = JSONObject()
                    fo.put("label", f.label)
                    fo.put("value", f.value)
                    fieldsArr.put(fo)
                }
                eo.put("fields", fieldsArr)
                entriesArr.put(eo)
            }
            co.put("entries", entriesArr)
            arr.put(co)
        }
        file.writeText(arr.toString(2))
    }

    /** Exporta el JSON actual hacia la Uri elegida por el usuario (ACTION_CREATE_DOCUMENT). */
    fun exportTo(uri: Uri) {
        if (!file.exists()) file.writeText("[]")
        val text = file.readText().ifBlank { "[]" }
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(text.toByteArray())
        }
    }

    /** Importa y reemplaza todos los datos desde la Uri elegida (ACTION_OPEN_DOCUMENT). */
    fun importFrom(uri: Uri): Boolean {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() } ?: return false

        return runCatching {
            JSONArray(text) // valida que sea un JSON valido antes de sobreescribir
            file.writeText(text)
            load()
            true
        }.getOrDefault(false)
    }

    fun newId(): String = UUID.randomUUID().toString()
}
