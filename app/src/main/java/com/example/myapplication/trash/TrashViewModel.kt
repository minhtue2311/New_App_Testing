package com.example.myapplication.trash

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Trash
import com.example.myapplication.repository.NoteRepository

class TrashViewModel : ViewModel() {
    private lateinit var noteRepository: NoteRepository
    private lateinit var liveDataTrash: LiveData<List<Trash>>

    fun getLiveDataTrash(context: Context): LiveData<List<Trash>> {
        noteRepository = NoteRepository()
        liveDataTrash = noteRepository.getAllTrash(context)
        return liveDataTrash
    }
}