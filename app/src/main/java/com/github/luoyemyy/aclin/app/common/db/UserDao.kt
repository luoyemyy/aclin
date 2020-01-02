package com.github.luoyemyy.aclin.app.common.db

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.luoyemyy.aclin.app.common.db.entity.User

@Dao
interface UserDao {

    @Insert
    fun insert(user: User)

    @Query("select * from user")
    fun getAll(): LiveData<List<User>>

    @Query("select * from user")
    fun getAll2(): DataSource.Factory<Int, User>
}