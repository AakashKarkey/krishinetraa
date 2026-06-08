package com.ace.krishinetra_mobile.data.repository

import android.content.Context
import android.net.Uri
import com.ace.krishinetra_mobile.data.model.PredictionResponse
import com.ace.krishinetra_mobile.data.remote.RetrofitClient
import com.ace.krishinetra_mobile.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AnalysisRepository(private val context: Context) {
    suspend fun analyzeImage(uri: Uri): Result<PredictionResponse> {
        return try {
            val file = ImageUtils.copyUriToCache(context, uri)
                ?: return Result.failure(Exception("Failed to process image"))

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val response = RetrofitClient.apiService.predictDisease(multipart)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
