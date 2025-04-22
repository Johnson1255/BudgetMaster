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

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalById(id: Long): Flow<Goal?>

    @Query("SELECT * FROM goals ORDER BY creationDate DESC")
    fun getAllGoals(): Flow<List<Goal>>

    // Optional: Query to update current amount for a goal
    @Query("UPDATE goals SET currentAmount = :newAmount WHERE id = :goalId")
    suspend fun updateGoalAmount(goalId: Long, newAmount: Double)
}
