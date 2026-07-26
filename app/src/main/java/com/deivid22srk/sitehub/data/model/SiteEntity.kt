package com.deivid22srk.sitehub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val faviconUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
