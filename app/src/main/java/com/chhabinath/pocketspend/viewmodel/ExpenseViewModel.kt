package com.chhabinath.pocketspend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chhabinath.pocketspend.data.Expense
import com.chhabinath.pocketspend.data.ExpenseDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val dao: ExpenseDao
) : ViewModel() {

    val expenses = dao.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalAmount = dao.getTotalAmount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addExpense(
        merchant: String,
        amount: Double,
        category: String,
        date: String
    ) {
        viewModelScope.launch {

            dao.insert(
                Expense(
                    merchant = merchant,
                    amount = amount,
                    category = category,
                    date = date
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            dao.delete(expense)
        }
    }
}

class ExpenseViewModelFactory(
    private val dao: ExpenseDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(dao) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}