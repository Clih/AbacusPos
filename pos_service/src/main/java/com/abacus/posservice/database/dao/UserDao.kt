package com.abacus.posservice.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.abacus.posservice.database.table.User

@Dao
interface UserDao : BaseDao<User> {

    @Query("select * from User")
    fun getAllUsers(): MutableList<User>?
}