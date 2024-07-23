package com.example.myapplication.model.relation

import androidx.room.Entity

@Entity(primaryKeys = ["idNote", "idCategory"])
data class NoteCategoryRef(
    val idNote : Int,
    val idCategory : Int,
)
