package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TvDao {
    @Query("SELECT * FROM tv_devices ORDER BY lastConnected DESC")
    fun getAllTvs(): Flow<List<TvEntity>>

    @Query("SELECT * FROM tv_devices WHERE id = :id LIMIT 1")
    suspend fun getTvById(id: Long): TvEntity?

    @Query("SELECT * FROM tv_devices WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTv(): TvEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTv(tv: TvEntity): Long

    @Update
    suspend fun updateTv(tv: TvEntity)

    @Delete
    suspend fun deleteTv(tv: TvEntity)

    @Query("UPDATE tv_devices SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE tv_devices SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultTv(id: Long)
}
