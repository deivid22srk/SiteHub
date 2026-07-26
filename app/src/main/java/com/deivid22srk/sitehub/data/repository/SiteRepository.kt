package com.deivid22srk.sitehub.data.repository

import com.deivid22srk.sitehub.data.db.SiteDao
import com.deivid22srk.sitehub.data.model.SiteEntity
import kotlinx.coroutines.flow.Flow

class SiteRepository(private val dao: SiteDao) {
    fun getAllSites(): Flow<List<SiteEntity>> = dao.getAllSites()

    suspend fun addSite(site: SiteEntity) = dao.insert(site)

    suspend fun updateSite(site: SiteEntity) = dao.update(site)

    suspend fun deleteSite(site: SiteEntity) = dao.delete(site)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun exists(url: String): Boolean = dao.exists(url)
}
