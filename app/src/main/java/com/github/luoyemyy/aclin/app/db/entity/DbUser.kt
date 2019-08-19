package com.github.luoyemyy.aclin.app.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DbUser(@PrimaryKey(autoGenerate = true) var id: Long, var name: String)