package com.spendsnap.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val photoUri: String,
    val timestamp: Long = System.currentTimeMillis()
)
