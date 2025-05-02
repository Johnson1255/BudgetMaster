package com.senlin.budgetmaster.data.repository

import com.senlin.budgetmaster.data.db.CategoryDao
import com.senlin.budgetmaster.data.db.GoalDao
import com.senlin.budgetmaster.data.db.TransactionDao
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull // Import firstOrNull
import java.time.LocalDate

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
        // If the transaction is linked to a goal, update the goal's current amount
        transaction.goalId?.let { goalId ->
            val amountToAdd = if (transaction.type == TransactionType.INCOME) {
                transaction.amount
            } else { // Assuming EXPENSE is the only other type affecting goals
                -transaction.amount // Subtract expense amount
            }
            goalDao.addAmountToGoal(goalId, amountToAdd)
        }
    }
    override suspend fun updateTransaction(transaction: Transaction) {
        // Get the old transaction state BEFORE updating
        val oldTransaction = transactionDao.getTransactionById(transaction.id).firstOrNull()

        // 1. Revert old transaction's effect on its goal (if any)
        oldTransaction?.goalId?.let { oldGoalId ->
            val amountToRevert = if (oldTransaction.type == TransactionType.INCOME) {
                -oldTransaction.amount // Subtract old income
            } else {
                oldTransaction.amount // Add back old expense
            }
            goalDao.addAmountToGoal(oldGoalId, amountToRevert)
        }

        // 2. Apply new transaction's effect on its goal (if any)
        transaction.goalId?.let { newGoalId ->
            val amountToAdd = if (transaction.type == TransactionType.INCOME) {
                transaction.amount // Add new income
            } else {
                -transaction.amount // Subtract new expense
            }
            goalDao.addAmountToGoal(newGoalId, amountToAdd)
        }

        // 3. Update the transaction itself
        transactionDao.updateTransaction(transaction)
    }
    override suspend fun deleteTransaction(transaction: Transaction) {
        // Revert the transaction's effect on its goal (if any) BEFORE deleting
        transaction.goalId?.let { goalId ->
            val amountToRevert = if (transaction.type == TransactionType.INCOME) {
                -transaction.amount // Subtract income
            } else {
                transaction.amount // Add back expense
            }
            goalDao.addAmountToGoal(goalId, amountToRevert)
        }

        // Delete the transaction itself
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
