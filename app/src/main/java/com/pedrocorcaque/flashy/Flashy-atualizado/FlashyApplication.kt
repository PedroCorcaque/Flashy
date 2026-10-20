package com.pedrocorcaque.flashy

import android.app.Application
import com.pedrocorcaque.flashy.data.local.FlashyDatabase
import com.pedrocorcaque.flashy.data.repository.FlashyRepository

class FlashyApplication : Application() {
    val database by lazy { FlashyDatabase.getDatabase(this) }
    val repository by lazy { FlashyRepository(database.dao()) }
}
