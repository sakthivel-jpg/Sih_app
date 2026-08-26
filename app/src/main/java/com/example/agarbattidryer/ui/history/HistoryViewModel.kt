package com.example.agarbattidryer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agarbattidryer.data.local.DryingBatchEntity
import com.example.agarbattidryer.data.repository.DryingHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: DryingHistoryRepository
) : ViewModel() {

    val historyBatches: StateFlow<List<DryingBatchEntity>> = historyRepository.observeHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            historyRepository.deleteBatch(batchId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    companion object {
        fun provideFactory(
            repository: DryingHistoryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(repository) as T
            }
        }
    }
}
