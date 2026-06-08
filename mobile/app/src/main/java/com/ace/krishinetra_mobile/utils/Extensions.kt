package com.ace.krishinetra_mobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream

fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        context.contentResolver.openInputStream(this)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        null
    }
}

fun Bitmap.rotateIfNeeded(context: Context, uri: Uri): Bitmap {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270f)
                else -> this
            }
        } ?: this
    } catch (e: Exception) {
        this
    }
}

fun Bitmap.rotateImage(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.toByteArray(quality: Int = 80): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

fun Bitmap.resizeBitmap(maxSize: Int): Bitmap {
    val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
    if (ratio >= 1f) return this
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}
