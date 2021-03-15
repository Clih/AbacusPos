package com.abacus.posservice.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abacus.posservice.database.dao.UserDao
import com.abacus.posservice.database.table.User

@Database(entities = [User::class], version = 1)
public abstract class MainDb : RoomDatabase() {

    abstract fun getMensesDao(): UserDao

    companion object {
        val instance = Single.sin

        @JvmStatic
        val DB_NAME = "abacus_pos.db"

        lateinit var mContext: Context
    }

    private object Single {
        val sin: MainDb = Room.databaseBuilder(
            mContext.applicationContext,
            MainDb::class.java,
            DB_NAME
        ).allowMainThreadQueries().build()
    }

}