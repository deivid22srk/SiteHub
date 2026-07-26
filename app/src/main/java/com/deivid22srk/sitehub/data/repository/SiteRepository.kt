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

    suspend fun getById(id: Long): SiteEntity? = dao.getById(id)

    suspend fun shareSession(siteId: Long, targetSiteId: Long) {
        val target = dao.getById(targetSiteId) ?: return
        val groupId = if (target.sharedGroupId > 0) target.sharedGroupId else targetSiteId
        dao.updateSharedGroup(siteId, groupId)
        if (target.sharedGroupId == 0L) {
            dao.updateSharedGroup(targetSiteId, groupId)
        }
    }

    suspend fun unshareSession(siteId: Long) {
        dao.updateSharedGroup(siteId, 0)
    }

    suspend fun getGroupPeers(groupId: Long, excludeId: Long): List<SiteEntity> =
        dao.getGroupPeers(groupId, excludeId)
}
