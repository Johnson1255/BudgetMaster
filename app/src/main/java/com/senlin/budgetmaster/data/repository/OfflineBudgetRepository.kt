package com.senlin.budgetmaster.data.repository

import com.senlin.budgetmaster.data.db.CategoryDao
import com.senlin.budgetmaster.data.db.GoalDao
import com.senlin.budgetmaster.data.db.TransactionDao
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate // Use LocalDate instead of Date

class OfflineBudgetRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao
) : BudgetRepository {

    // Transaction Operations
    override fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    override fun getTransactionById(id: Long): Flow<Transaction?> = transactionDao.getTransactionById(id)
    override fun getTransactionsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> = // Use LocalDate
        transactionDao.getTransactionsBetweenDates(startDate, endDate)
    override fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategoryId(categoryId)
    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }
    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    // Category Operations
    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override fun getCategoryById(id: Long): Flow<Category?> = categoryDao.getCategoryById(id)
    override fun getCategoryByName(name: String): Flow<Category?> = categoryDao.getCategoryByName(name)
    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }
    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Goal Operations
    override fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()
    override fun getGoalById(id: Long): Flow<Goal?> = goalDao.getGoalById(id)
    override suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }
    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }
    override suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
    override suspend fun updateGoalAmount(goalId: Long, newAmount: Double) {
        goalDao.updateGoalAmount(goalId, newAmount)
    }
}
