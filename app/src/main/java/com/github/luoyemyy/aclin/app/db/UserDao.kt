package com.github.luoyemyy.aclin.app.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.luoyemyy.aclin.app.api.entity.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Query("select * from user")
    fun getAll(): LiveData<List<User>>
}