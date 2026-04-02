package com.spendsnap.app

import android.app.Application
import com.spendsnap.app.data.AppDatabase
import com.spendsnap.app.data.ExpenseRepository

class App : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
}
