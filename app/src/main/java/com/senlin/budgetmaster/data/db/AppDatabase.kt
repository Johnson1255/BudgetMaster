package com.senlin.budgetmaster.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.AutoMigration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.User

@Database(
    entities = [Transaction::class, Category::class, Goal::class, User::class],
    version = 3, // Incremented version due to schema change
    exportSchema = true // Export schema for auto-migration verification
    // Removed autoMigrations as 1.json is missing
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
    abstract fun userDao(): UserDao

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
                // Allow Room to destructively recreate database tables if Migrations are not available.
                .fallbackToDestructiveMigration()
                // Room handles the migration automatically based on the annotation
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
