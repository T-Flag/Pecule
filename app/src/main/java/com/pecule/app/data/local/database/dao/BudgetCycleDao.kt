package com.pecule.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetCycleDao {

    @Insert
    suspend fun insert(cycle: BudgetCycle): Long

    @Update
    suspend fun update(cycle: BudgetCycle)

    @Delete
    suspend fun delete(cycle: BudgetCycle)

    @Query("SELECT * FROM budget_cycles WHERE id = :id")
    fun getById(id: Long): Flow<BudgetCycle?>

    @Query("SELECT * FROM budget_cycles WHERE endDate IS NULL LIMIT 1")
    fun getCurrentCycle(): Flow<BudgetCycle?>

    @Query("SELECT * FROM budget_cycles ORDER BY startDate DESC")
    fun getAllCycles(): Flow<List<BudgetCycle>>
}
