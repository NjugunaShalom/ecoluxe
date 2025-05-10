package com.example.ecoluxe.data.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun uploadImageToImgur(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val fileName = if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.getString(nameIndex)
            } else {
                "temp.jpg"
            }
            cursor?.close()

            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(context.cacheDir, fileName)
            file.outputStream().use { inputStream.copyTo(it) }

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

            val response = RetrofitClient.imgurService.uploadImage(
                image = multipartBody,
                auth = "Client-ID 0c64c2245412884"
            )

            if (response.isSuccessful) {
                response.body()?.data?.link
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
