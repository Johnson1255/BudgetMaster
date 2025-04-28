package com.senlin.budgetmaster.data.db

import androidx.room.TypeConverter
import com.senlin.budgetmaster.data.model.TransactionType
// Removed java.util.Date import
import java.time.LocalDate // Import LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? {
        return date?.toEpochDay()
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
