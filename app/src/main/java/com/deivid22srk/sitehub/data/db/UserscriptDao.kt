package com.deivid22srk.sitehub.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deivid22srk.sitehub.data.model.UserscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserscriptDao {
    @Query("SELECT * FROM userscripts WHERE siteId = :siteId ORDER BY importedAt DESC")
    fun getBySiteId(siteId: Long): Flow<List<UserscriptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: UserscriptEntity)

    @Update
    suspend fun update(script: UserscriptEntity)

    @Delete
    suspend fun delete(script: UserscriptEntity)

    @Query("DELETE FROM userscripts WHERE siteId = :siteId")
    suspend fun deleteBySiteId(siteId: Long)
}
