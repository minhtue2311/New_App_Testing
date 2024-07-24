package com.example.myapplication.note.noteViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDAO
import com.example.myapplication.repository.NoteRepository

class NoteViewModel() : ViewModel() {
    private lateinit var noteRepository: NoteRepository
    private lateinit var liveDataNote: LiveData<List<Note>>
    private val mediatorNotes = MediatorLiveData<List<Note>>()
    private var currentSource: LiveData<*>? = null // Variable to hold current source

    // Public LiveData for observing notes
    val notes: LiveData<List<Note>> get() = mediatorNotes

    fun getLiveDataNote(context: Context) {
        noteRepository = NoteRepository()
        val allNotes = noteRepository.getAllNote(context)
        currentSource = allNotes
        mediatorNotes.addSource(allNotes) { notesList ->
            mediatorNotes.value = notesList
        }
    }

    fun getNoteByCategories(categoryId: Int, noteDao: NoteDAO) {
        val notesByCategory = noteDao.getCategoryWithNotes(categoryId)
        currentSource = notesByCategory
        mediatorNotes.addSource(notesByCategory) { categoryWithNotes ->
            mediatorNotes.value = categoryWithNotes?.listNote ?: emptyList()
        }
    }

    fun getUncategorizedNote(noteDao: NoteDAO) {
        val uncategorizedNotes = noteDao.getNotesWithoutCategories()
        currentSource = uncategorizedNotes
        mediatorNotes.addSource(uncategorizedNotes) { notesList ->
            mediatorNotes.value = notesList
        }
    }

    // Method to clear previous sources
    fun clearSources() {
       currentSource?.let {
           mediatorNotes.removeSource(it)
           currentSource = null
       }
    }
}
