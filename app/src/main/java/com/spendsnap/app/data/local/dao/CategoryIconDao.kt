package com.spendsnap.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendsnap.app.data.local.entities.CategoryIconEntity

@Dao
interface CategoryIconDao {
    @Query("SELECT * FROM category_icons")
    suspend fun getCategoryIcons(): List<CategoryIconEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryIcons(icons: List<CategoryIconEntity>)

    @Query("DELETE FROM category_icons")
    suspend fun clearCategoryIcons()
}
