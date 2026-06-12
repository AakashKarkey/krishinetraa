package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ace.krishinetra_mobile.data.local.AppDatabase
import com.ace.krishinetra_mobile.data.model.AnalysisRecord
import com.ace.krishinetra_mobile.data.repository.HistoryRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistoryRepository(
        AppDatabase.getDatabase(application).analysisDao()
    )

    val records: LiveData<List<AnalysisRecord>> = MutableLiveData()

    init {
        viewModelScope.launch {
            repository.getAllRecords().collectLatest { list ->
                (records as MutableLiveData).postValue(list)
            }
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
        }
    }
}
