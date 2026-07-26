package com.deivid22srk.sitehub.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deivid22srk.sitehub.data.model.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY addedAt DESC")
    fun getAllSites(): Flow<List<SiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(site: SiteEntity)

    @Update
    suspend fun update(site: SiteEntity)

    @Delete
    suspend fun delete(site: SiteEntity)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM sites WHERE url = :url)")
    suspend fun exists(url: String): Boolean

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getById(id: Long): SiteEntity?

    @Query("UPDATE sites SET sharedGroupId = :groupId WHERE id = :siteId")
    suspend fun updateSharedGroup(siteId: Long, groupId: Long)

    @Query("SELECT * FROM sites WHERE sharedGroupId = :groupId AND id != :excludeId")
    suspend fun getGroupPeers(groupId: Long, excludeId: Long): List<SiteEntity>
}
