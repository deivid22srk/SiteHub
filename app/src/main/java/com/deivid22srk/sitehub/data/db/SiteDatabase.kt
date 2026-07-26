package com.deivid22srk.sitehub.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deivid22srk.sitehub.data.model.SiteEntity
import com.deivid22srk.sitehub.data.model.UserscriptEntity

@Database(entities = [SiteEntity::class, UserscriptEntity::class], version = 2, exportSchema = false)
abstract class SiteDatabase : RoomDatabase() {
    abstract fun siteDao(): SiteDao
    abstract fun userscriptDao(): UserscriptDao

    companion object {
        @Volatile
        private var INSTANCE: SiteDatabase? = null

        fun getInstance(context: Context): SiteDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SiteDatabase::class.java,
                    "sitehub.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
