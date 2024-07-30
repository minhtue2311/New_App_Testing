package com.example.myapplication.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.Trash


class NoteRepository {
    fun getAllNote(context : Context) : LiveData<List<Note>> {
        return NoteDatabase.getInstance(context).noteDao().getAllNote()
    }
    fun getAllTrash(context: Context) : LiveData<List<Trash>>{
        return NoteDatabase.getInstance(context).noteDao().getAllNoteFromTrash()
    }
}
