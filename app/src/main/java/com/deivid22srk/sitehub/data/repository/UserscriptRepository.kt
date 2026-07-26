package com.deivid22srk.sitehub.data.repository

import com.deivid22srk.sitehub.data.db.UserscriptDao
import com.deivid22srk.sitehub.data.model.UserscriptEntity
import kotlinx.coroutines.flow.Flow

class UserscriptRepository(private val dao: UserscriptDao) {
    fun getBySiteId(siteId: Long): Flow<List<UserscriptEntity>> = dao.getBySiteId(siteId)

    suspend fun insert(script: UserscriptEntity) = dao.insert(script)

    suspend fun update(script: UserscriptEntity) = dao.update(script)

    suspend fun delete(script: UserscriptEntity) = dao.delete(script)

    suspend fun deleteBySiteId(siteId: Long) = dao.deleteBySiteId(siteId)
}
