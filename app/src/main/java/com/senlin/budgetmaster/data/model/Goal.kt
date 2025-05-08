package com.senlin.budgetmaster.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
// import androidx.room.TypeConverters // Likely applied at DB level
import java.time.LocalDate // Import LocalDate

// Removed TODO

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "name"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // Foreign key to User table
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: LocalDate? = null, // Use LocalDate
    val creationDate: LocalDate // Use LocalDate
)
