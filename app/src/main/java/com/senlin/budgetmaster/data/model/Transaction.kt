package com.senlin.budgetmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
// TypeConverters annotation might not be needed here if applied at the Database level
// import androidx.room.TypeConverters
import java.time.LocalDate // Import LocalDate

// Removed TODO as converter is handled

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long, // Foreign key to Category table
    val goalId: Long? = null, // Foreign key to Goal table (nullable)
    val date: LocalDate, // Use LocalDate
    val note: String? = null
)
