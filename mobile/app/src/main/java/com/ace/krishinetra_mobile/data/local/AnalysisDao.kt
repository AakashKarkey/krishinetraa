package com.ace.krishinetra_mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ace.krishinetra_mobile.data.model.AnalysisRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analysis_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Insert
    suspend fun insertRecord(record: AnalysisRecord)

    @Query("DELETE FROM analysis_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM analysis_records")
    suspend fun deleteAllRecords()
}
