package com.deivid22srk.sitehub.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deivid22srk.sitehub.data.model.SiteEntity

@Database(entities = [SiteEntity::class], version = 1, exportSchema = false)
abstract class SiteDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao

    companion object {
        @Volatile
        private var INSTANCE: SiteDatabase? = null

        fun getInstance(context: Context): SiteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SiteDatabase::class.java,
                    "sitehub.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
