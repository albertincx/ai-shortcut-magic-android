package com.example.shortcutmagic

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val launchIntent: android.content.Intent?
)