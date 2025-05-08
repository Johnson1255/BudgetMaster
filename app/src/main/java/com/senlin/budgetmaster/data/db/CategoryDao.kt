package com.senlin.budgetmaster.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.senlin.budgetmaster.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id AND userId = :userId")
    fun getCategoryById(id: Long, userId: Long): Flow<Category?>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: Long): Flow<List<Category>>

    // Optional: Query to find category by name for a specific user
    @Query("SELECT * FROM categories WHERE name = :name AND userId = :userId LIMIT 1")
    fun getCategoryByName(name: String, userId: Long): Flow<Category?>
}
