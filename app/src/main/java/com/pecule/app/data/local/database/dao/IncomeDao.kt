package com.pecule.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pecule.app.data.local.database.entity.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Insert
    suspend fun insert(income: Income): Long

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM incomes WHERE id = :id")
    fun getById(id: Long): Flow<Income?>

    @Query("SELECT * FROM incomes WHERE cycleId = :cycleId ORDER BY date DESC")
    fun getByCycleId(cycleId: Long): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE cycleId = :cycleId AND isFixed = 1 ORDER BY date DESC")
    fun getFixedIncomes(cycleId: Long): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE cycleId = :cycleId AND isFixed = 0 ORDER BY date DESC")
    fun getVariableIncomes(cycleId: Long): Flow<List<Income>>
}
