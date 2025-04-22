package com.senlin.budgetmaster.data.repository

import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface BudgetRepository {

    // Transaction Operations
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionById(id: Long): Flow<Transaction?>
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)

    // Category Operations
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: Long): Flow<Category?>
    fun getCategoryByName(name: String): Flow<Category?>
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)

    // Goal Operations
    fun getAllGoals(): Flow<List<Goal>>
    fun getGoalById(id: Long): Flow<Goal?>
    suspend fun insertGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    suspend fun updateGoalAmount(goalId: Long, newAmount: Double)
}
