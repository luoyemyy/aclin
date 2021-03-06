package com.github.luoyemyy.aclin.app.common.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(@PrimaryKey(autoGenerate = true) var id: Long, var name: String)