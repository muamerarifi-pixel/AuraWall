package com.aurawall.app.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class WallpaperTarget(val label: String, val flag: Int) {
    HOME("Home Screen", WallpaperManager.FLAG_SYSTEM),
    LOCK("Lock Screen", WallpaperManager.FLAG_LOCK),
    BOTH("Home & Lock Screen", WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
}

object WallpaperApplier {

    suspend fun applyWallpaper(
        context: Context,
        imageUrl: String,
        target: WallpaperTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = downloadBitmap(imageUrl)
                ?: error("Could not download wallpaper image")
            val manager = WallpaperManager.getInstance(context)
            manager.setBitmap(bitmap, null, true, target.flag)
            bitmap.recycle()
        }
    }

    suspend fun saveToGallery(
        context: Context,
        imageUrl: String,
        fileName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = downloadBitmap(imageUrl)
                ?: error("Could not download wallpaper image")
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AuraWall")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Failed to create MediaStore entry")

            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            } ?: error("Failed to open output stream")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            bitmap.recycle()
        }
    }

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            doInput = true
            connect()
        }
        return connection.inputStream.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }
}
