package com.example.myapplication.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note

data class NoteWithCategories(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "idNote",
        entityColumn = "idCategory",
        associateBy = Junction(NoteCategoryRef::class)
    )
    val listCategories: List<Categories>
)