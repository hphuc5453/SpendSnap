package com.spendsnap.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spendsnap.app.data.local.dao.UserDao
import com.spendsnap.app.data.local.entities.UserEntity

@Database(entities = [Expense::class, UserEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao
}
