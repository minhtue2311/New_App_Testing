package com.example.myapplication.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note

data class CategoryWithNotes(
    @Embedded val category : Categories,
    @Relation(
        parentColumn = "idCategory",
        entityColumn = "idNote",
        associateBy = Junction(NoteCategoryRef::class)
    )
    val listNote : List<Note>
)
