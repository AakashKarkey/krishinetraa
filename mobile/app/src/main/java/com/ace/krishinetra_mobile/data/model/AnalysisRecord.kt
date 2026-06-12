package com.ace.krishinetra_mobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_records")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diseaseName: String,
    val confidence: Int,
    val description: String,
    val treatment: String,
    val preventionTips: String,
    val message: String?,
    val greenRatio: Double?,
    val imageUri: String?,
    val timestamp: Long = System.currentTimeMillis()
)
