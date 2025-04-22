package com.senlin.budgetmaster.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction

@Database(
    entities = [Transaction::class, Category::class, Goal::class],
    version = 1, // Start with version 1. Increment if schema changes.
    exportSchema = false // Optional: Set to true to export schema to a folder
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_master_database"
                )
                // Optional: Add migrations if needed later
                // .addMigrations(MIGRATION_1_2)
                // Optional: Prepopulate database on creation
                // .addCallback(roomDatabaseCallback)
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        // Optional: Callback to prepopulate data
        /*
        private val roomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // If you want to keep data through app restarts,
                // comment out the following block
                INSTANCE?.let { database ->
                    // CoroutineScope(Dispatchers.IO).launch {
                    //     populateDatabase(database.categoryDao())
                    // }
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            // Add default categories
            categoryDao.insertCategory(Category(name = "Salary"))
            categoryDao.insertCategory(Category(name = "Food"))
            categoryDao.insertCategory(Category(name = "Transport"))
            // Add more default categories as needed
        }
        */
    }
}
