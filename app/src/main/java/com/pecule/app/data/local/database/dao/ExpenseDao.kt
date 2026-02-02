package com.pecule.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pecule.app.data.local.database.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getById(id: Long): Flow<Expense?>

    @Query("SELECT * FROM expenses WHERE cycleId = :cycleId ORDER BY date DESC")
    fun getByCycleId(cycleId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE cycleId = :cycleId AND isFixed = 1 ORDER BY date DESC")
    fun getFixedExpenses(cycleId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE cycleId = :cycleId AND isFixed = 0 ORDER BY date DESC")
    fun getVariableExpenses(cycleId: Long): Flow<List<Expense>>
}
