package com.example.shortcutmagic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import java.util.*

object ShortcutHelper {

    fun pinShortcut(context: Context, name: String, type: String, data: String, customIcon: Bitmap? = null) {
        val intent = when (type) {
            "URL" -> {
                val finalUrl = if (!data.startsWith("http://") && !data.startsWith("https://")) {
                    "https://$data"
                } else data
                Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            }
            "FILE" -> {
                val uri = Uri.parse(data)
                Intent(Intent.ACTION_VIEW).apply {
                    val mimeType = context.contentResolver.getType(uri) ?: "*/*"
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "APP" -> {
                context.packageManager.getLaunchIntentForPackage(data) ?: return
            }
            else -> return
        }

        val icon = if (customIcon != null) {
            IconCompat.createWithBitmap(customIcon)
        } else if (type == "APP") {
            try {
                val appIcon = context.packageManager.getApplicationIcon(data)
                IconCompat.createWithBitmap(drawableToBitmap(appIcon))
            } catch (e: Exception) {
                IconCompat.createWithResource(context, R.mipmap.ic_launcher)
            }
        } else {
            IconCompat.createWithResource(context, R.mipmap.ic_launcher)
        }

        val shortcut = ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
            .setShortLabel(name)
            .setIcon(icon)
            .setIntent(intent)
            .build()

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
            Toast.makeText(context, R.string.msg_shortcut_created, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Pinned shortcuts are not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) return drawable.bitmap
        val bitmap = Bitmap.createBitmap(
            if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1,
            if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}