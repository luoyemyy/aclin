package com.github.luoyemyy.aclin.app.common.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.luoyemyy.aclin.app.App
import com.github.luoyemyy.aclin.app.common.db.entity.User

@Database(entities = arrayOf(User::class), version = 1)
abstract class Db : RoomDatabase() {

    companion object {
        private lateinit var db: Db
        fun initDb(app: App) {
            db = Room.databaseBuilder(app, Db::class.java, "db").build()
        }
        fun getInstance(): Db = db
    }

    abstract fun userDao(): UserDao
}


fun getUserDao() = Db.getInstance().userDao()