package com.example.myapplication.model.interface_model

import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note

interface InterfaceOnClickListener {
    fun onClickItemNoteListener(note : Note)
    fun onClickColorItem(color : String)
    fun onSelectedNote(listNoteSelectedResult : ArrayList<Note>)
    fun onClickCategoriesItem(categories: Categories)
}