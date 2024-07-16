package com.example.myapplication.note.noteViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Note
import com.example.myapplication.repository.NoteRepository

class NoteViewModel() : ViewModel() {
    private lateinit var noteRepository: NoteRepository
    private lateinit var liveDataNote: LiveData<List<Note>>

    fun getLiveDataNote(context: Context): LiveData<List<Note>> {
        noteRepository = NoteRepository()
        liveDataNote = noteRepository.getAllNote(context)
        return liveDataNote
    }
}
