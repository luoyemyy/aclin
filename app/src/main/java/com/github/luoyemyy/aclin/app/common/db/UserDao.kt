package com.github.luoyemyy.aclin.app.common.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.luoyemyy.aclin.app.common.db.entity.DbUser

@Dao
interface UserDao {

    @Insert
    fun insert(user: DbUser)

    @Query("select * from user")
    fun getAll(): LiveData<List<DbUser>>
}