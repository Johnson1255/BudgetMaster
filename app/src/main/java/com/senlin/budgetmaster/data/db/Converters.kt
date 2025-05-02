package com.senlin.budgetmaster.data.db

import androidx.room.TypeConverter
import com.senlin.budgetmaster.data.model.TransactionType
// Removed java.util.Date import
import java.time.Instant // Import Instant
import java.time.LocalDate // Import LocalDate
import java.time.ZoneId // Import ZoneId

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        // Convert milliseconds since epoch to LocalDate
        return value?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        // Convert LocalDate to milliseconds since epoch at the start of the day in the system's default timezone
        return date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun fromTransactionTypeName(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }

    @TypeConverter
    fun transactionTypeToName(transactionType: TransactionType?): String? {
        return transactionType?.name
    }
}
