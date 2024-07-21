package com.example.myapplication.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDAO {
    @Insert
    fun insertNote(note: Note)

    @Update
    fun updateNote(note: Note)

    @Query("SELECT * FROM Note")
    fun getAllNote() : LiveData<List<Note>>

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM NOTE WHERE categoryId = :categoryId")
    fun getNotesByCategoryID(categoryId :Int) : LiveData<List<Note>>

}

@Dao
interface CategoriesDAO{
    @Insert
    fun insertCategories(categories : Categories)

    @Update
    fun updateCategories(categories : Categories)

    @Query("SELECT * FROM Categories")
    fun getAllCategories() : LiveData<List<Categories>>

    @Delete
    fun deleteCategory(categories : Categories)
}