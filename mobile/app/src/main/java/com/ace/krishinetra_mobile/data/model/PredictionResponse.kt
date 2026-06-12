package com.ace.krishinetra_mobile.data.model

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("class")
    val diseaseClass: String?,

    @SerializedName("disease")
    val disease: String?,

    val confidence: Double,

    val probabilities: Map<String, Double>?,

    val model: String?,

    @SerializedName("processing_time_s")
    val processingTimeS: Double?,

    val description: String?,

    val treatment: String?,

    @SerializedName("prevention_tips")
    val preventionTips: List<String>?,

    val message: String?,

    @SerializedName("green_ratio")
    val greenRatio: Double?
) {
    val diseaseName: String
        get() = disease ?: diseaseClass ?: "Unknown"

    val confidencePercent: Int
        get() = if (confidence <= 1.0) (confidence * 100).toInt() else confidence.toInt()
}
