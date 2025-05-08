package com.senlin.budgetmaster.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.senlin.budgetmaster.data.model.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE id = :id AND userId = :userId")
    fun getGoalById(id: Long, userId: Long): Flow<Goal?>

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY creationDate DESC")
    fun getAllGoals(userId: Long): Flow<List<Goal>>

    @Query("UPDATE goals SET currentAmount = currentAmount + :amount WHERE id = :goalId AND userId = :userId")
    suspend fun addAmountToGoal(goalId: Long, amount: Double, userId: Long)

    // Optional: Query to update current amount for a goal
    @Query("UPDATE goals SET currentAmount = :newAmount WHERE id = :goalId AND userId = :userId")
    suspend fun updateGoalAmount(goalId: Long, newAmount: Double, userId: Long)
}
