package com.senlin.budgetmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

// TODO: Define TypeConverter for Date

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
    val date: Date,
    val note: String? = null
)
