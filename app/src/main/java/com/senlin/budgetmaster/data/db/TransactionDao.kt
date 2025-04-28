package com.senlin.budgetmaster.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.senlin.budgetmaster.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate // Import LocalDate
// Remove java.util.Date import

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> // Use LocalDate

    // Add more specific queries as needed, e.g., by type or category
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>>
}
