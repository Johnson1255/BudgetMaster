package com.senlin.budgetmaster.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
// TypeConverters annotation might not be needed here if applied at the Database level
// import androidx.room.TypeConverters
import java.time.LocalDate // Import LocalDate

// Removed TODO as converter is handled

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"]),
        Index(value = ["goalId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"], // Assuming Category primary key is 'id'
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Or SET_NULL, depending on desired behavior
        ),
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"], // Assuming Goal primary key is 'id'
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL // Transactions can exist without a goal
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // Foreign key to User table
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long, // Foreign key to Category table
    val goalId: Long? = null, // Foreign key to Goal table (nullable)
    val date: LocalDate, // Use LocalDate
    val note: String? = null
)
