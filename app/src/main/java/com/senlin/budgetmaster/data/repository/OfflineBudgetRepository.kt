package com.senlin.budgetmaster.data.repository

import com.senlin.budgetmaster.data.db.CategoryDao
import com.senlin.budgetmaster.data.db.GoalDao
import com.senlin.budgetmaster.data.db.TransactionDao
import com.senlin.budgetmaster.data.db.UserDao
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class OfflineBudgetRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao,
    private val userDao: UserDao
) : BudgetRepository {

    // User Operations
    override suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    override suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    override suspend fun getUserById(userId: Long): User? = userDao.getUserById(userId)

    // Transaction Operations
    override fun getAllTransactions(userId: Long): Flow<List<Transaction>> = transactionDao.getAllTransactions(userId)
    override fun getTransactionById(id: Long, userId: Long): Flow<Transaction?> = transactionDao.getTransactionById(id, userId)
    override fun getTransactionsBetweenDates(userId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetweenDates(userId, startDate, endDate)
    override fun getTransactionsByCategoryId(userId: Long, categoryId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCategoryId(userId, categoryId)

    override suspend fun insertTransaction(transaction: Transaction) {
        if (transaction.userId == 0L) {
            println("Error: Attempted to insert transaction with invalid userId: ${transaction.userId}")
            return
        }
        transactionDao.insertTransaction(transaction)
        transaction.goalId?.let { goalId ->
            val amountToAdd = if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            goalDao.addAmountToGoal(goalId, amountToAdd, transaction.userId)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        if (transaction.userId == 0L) {
            println("Error: Attempted to update transaction with invalid userId: ${transaction.userId}")
            return
        }
        val oldTransaction = transactionDao.getTransactionById(transaction.id, transaction.userId).firstOrNull()

        oldTransaction?.goalId?.let { oldGoalId ->
            if (oldTransaction.userId == transaction.userId) {
                val amountToRevert = if (oldTransaction.type == TransactionType.INCOME) -oldTransaction.amount else oldTransaction.amount
                goalDao.addAmountToGoal(oldGoalId, amountToRevert, oldTransaction.userId)
            }
        }

        transaction.goalId?.let { newGoalId ->
            val amountToAdd = if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            goalDao.addAmountToGoal(newGoalId, amountToAdd, transaction.userId)
        }
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        if (transaction.userId == 0L) {
            println("Error: Attempted to delete transaction with invalid userId: ${transaction.userId}")
            return
        }
        transaction.goalId?.let { goalId ->
            val amountToRevert = if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
            goalDao.addAmountToGoal(goalId, amountToRevert, transaction.userId)
        }
        transactionDao.deleteTransaction(transaction)
    }

    // Category Operations
    override fun getAllCategories(userId: Long): Flow<List<Category>> = categoryDao.getAllCategories(userId)
    override fun getCategoryById(id: Long, userId: Long): Flow<Category?> = categoryDao.getCategoryById(id, userId)
    override fun getCategoryByName(name: String, userId: Long): Flow<Category?> = categoryDao.getCategoryByName(name, userId)

    override suspend fun insertCategory(category: Category) {
        if (category.userId == 0L) {
            println("Error: Attempted to insert category with invalid userId: ${category.userId}")
            return
        }
        categoryDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        if (category.userId == 0L) {
            println("Error: Attempted to update category with invalid userId: ${category.userId}")
            return
        }
        categoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: Category) {
        if (category.userId == 0L) {
            println("Error: Attempted to delete category with invalid userId: ${category.userId}")
            return
        }
        categoryDao.deleteCategory(category)
    }

    // Goal Operations
    override fun getAllGoals(userId: Long): Flow<List<Goal>> = goalDao.getAllGoals(userId)
    override fun getGoalById(id: Long, userId: Long): Flow<Goal?> = goalDao.getGoalById(id, userId)

    override suspend fun insertGoal(goal: Goal) {
        if (goal.userId == 0L) {
            println("Error: Attempted to insert goal with invalid userId: ${goal.userId}")
            return
        }
        goalDao.insertGoal(goal)
    }

    override suspend fun updateGoal(goal: Goal) {
        if (goal.userId == 0L) {
            println("Error: Attempted to update goal with invalid userId: ${goal.userId}")
            return
        }
        goalDao.updateGoal(goal)
    }

    override suspend fun deleteGoal(goal: Goal) {
        if (goal.userId == 0L) {
            println("Error: Attempted to delete goal with invalid userId: ${goal.userId}")
            return
        }
        goalDao.deleteGoal(goal)
    }

    override suspend fun updateGoalAmount(goalId: Long, newAmount: Double, userId: Long) {
        if (userId == 0L) {
            println("Error: Attempted to update goal amount with invalid userId: $userId")
            return
        }
        goalDao.updateGoalAmount(goalId, newAmount, userId)
    }

    override suspend fun addAmountToGoal(goalId: Long, amount: Double, userId: Long) {
        if (userId == 0L) {
            println("Error: Attempted to add amount to goal with invalid userId: $userId")
            return
        }
        goalDao.addAmountToGoal(goalId, amount, userId)
    }
}
