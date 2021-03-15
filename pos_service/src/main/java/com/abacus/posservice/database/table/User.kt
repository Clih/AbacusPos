package com.abacus.posservice.database.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User")
data class User(
    @PrimaryKey()
    var id: String?,
    @ColumnInfo(name = "u_name")
    var userName: String?,
    @ColumnInfo(name = "u_sex")
    var userSex: Int = 1,
    @ColumnInfo(name = "u_icon")
    var userIcon: String?
)