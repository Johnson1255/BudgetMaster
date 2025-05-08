package com.senlin.budgetmaster.data.repository

import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate // Import LocalDate
// Remove java.util.Date import

interface BudgetRepository {

    // User Operations
    suspend fun insertUser(user: User): Long
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(userId: Long): User?

    // Transaction Operations
    fun getAllTransactions(userId: Long): Flow<List<Transaction>>
    fun getTransactionById(id: Long, userId: Long): Flow<Transaction?>
    fun getTransactionsBetweenDates(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> // Use LocalDate
    fun getTransactionsByCategoryId(userId: Long, categoryId: Long): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction) // Assumes transaction.userId is set
    suspend fun updateTransaction(transaction: Transaction) // Assumes transaction.userId is set
    suspend fun deleteTransaction(transaction: Transaction) // Assumes transaction.userId is set

    // Category Operations
    fun getAllCategories(userId: Long): Flow<List<Category>>
    fun getCategoryById(id: Long, userId: Long): Flow<Category?>
    fun getCategoryByName(name: String, userId: Long): Flow<Category?>
    suspend fun insertCategory(category: Category) // Assumes category.userId is set
    suspend fun updateCategory(category: Category) // Assumes category.userId is set
    suspend fun deleteCategory(category: Category) // Assumes category.userId is set

    // Goal Operations
    fun getAllGoals(userId: Long): Flow<List<Goal>>
    fun getGoalById(id: Long, userId: Long): Flow<Goal?>
    suspend fun insertGoal(goal: Goal) // Assumes goal.userId is set
    suspend fun updateGoal(goal: Goal) // Assumes goal.userId is set
    suspend fun deleteGoal(goal: Goal) // Assumes goal.userId is set
    suspend fun updateGoalAmount(goalId: Long, newAmount: Double, userId: Long)
    suspend fun addAmountToGoal(goalId: Long, amount: Double, userId: Long) // Added from GoalDao
}
