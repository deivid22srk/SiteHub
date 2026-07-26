package com.deivid22srk.sitehub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "userscripts")
data class UserscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val name: String,
    val version: String,
    val description: String,
    val matchPatterns: String,
    val scriptContent: String,
    val enabled: Boolean = true,
    val importedAt: Long = System.currentTimeMillis()
)
