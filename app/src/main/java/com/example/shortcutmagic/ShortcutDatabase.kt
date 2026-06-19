package com.example.shortcutmagic

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ShortcutEntry(
    val id: String,
    val name: String,
    val type: String,
    val data: String,
    val iconPath: String? = null
)

class ShortcutStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shortcuts_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveShortcut(shortcut: ShortcutEntry) {
        val shortcuts = getAllShortcuts().toMutableList()
        shortcuts.add(shortcut)
        val json = gson.toJson(shortcuts)
        prefs.edit().putString("shortcuts_list", json).apply()
    }

    fun getAllShortcuts(): List<ShortcutEntry> {
        val json = prefs.getString("shortcuts_list", null) ?: return emptyList()
        val type = object : TypeToken<List<ShortcutEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearAll() {
        prefs.edit().remove("shortcuts_list").apply()
    }
}