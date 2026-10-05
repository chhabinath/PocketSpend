package com.chhabinath.pocketspend.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val merchant: String,
    val amount: Double,
    val category: String,
    val date: String,
    val source: String = "manual"
)