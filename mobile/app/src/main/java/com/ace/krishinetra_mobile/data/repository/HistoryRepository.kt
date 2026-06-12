package com.ace.krishinetra_mobile.data.repository

import com.ace.krishinetra_mobile.data.local.AnalysisDao
import com.ace.krishinetra_mobile.data.model.AnalysisRecord
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: AnalysisDao) {
    fun getAllRecords(): Flow<List<AnalysisRecord>> = dao.getAllRecords()

    suspend fun insertRecord(record: AnalysisRecord) {
        dao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteRecord(id)
    }

    suspend fun deleteAllRecords() {
        dao.deleteAllRecords()
    }
}
