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
}