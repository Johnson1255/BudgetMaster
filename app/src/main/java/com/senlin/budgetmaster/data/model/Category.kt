package com.senlin.budgetmaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    // Optional: Add icon identifier or color for visual representation
    // val iconResId: Int? = null,
    // val colorHex: String? = null
)
