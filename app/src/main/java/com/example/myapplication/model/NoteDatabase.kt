package com.example.myapplication.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
    @Database(entities = [Note::class, Categories ::class], version = 1)
    abstract class NoteDatabase : RoomDatabase() {
        companion object {

            @Volatile
            private var instance: NoteDatabase? = null

            @Synchronized
            fun getInstance(context: Context?): NoteDatabase {
                if (instance == null) {
                    instance =
                        Room.databaseBuilder(context!!, NoteDatabase::class.java, "NoteDatabase_2.db")
                            .allowMainThreadQueries().build()
                }
                return instance!!
            }
        }

        abstract fun noteDao(): NoteDAO
        abstract fun categoriesDao() : CategoriesDAO
    }
