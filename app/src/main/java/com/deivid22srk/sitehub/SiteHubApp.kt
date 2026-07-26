package com.deivid22srk.sitehub

import android.app.Application
import com.deivid22srk.sitehub.data.db.SiteDatabase
import com.deivid22srk.sitehub.data.repository.SiteRepository
import com.deivid22srk.sitehub.data.repository.UserscriptRepository

class SiteHubApp : Application() {
    val database by lazy { SiteDatabase.getInstance(this) }
    val repository by lazy { SiteRepository(database.siteDao()) }
    val userscriptRepository by lazy { UserscriptRepository(database.userscriptDao()) }
}
