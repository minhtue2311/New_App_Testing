package com.example.myapplication.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase


class NoteRepository {
    fun getAllNote(context : Context) : LiveData<List<Note>> {
        return NoteDatabase.getInstance(context).noteDao().getAllNote()
    }
}
