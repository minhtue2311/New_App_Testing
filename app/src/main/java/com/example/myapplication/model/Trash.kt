package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Trash")
data class Trash(
    @PrimaryKey(autoGenerate = true)
    var idNoteTrash : Int? = null,
    var title : String,
    var content: String,
    var createDate: String,
    var editedDate: String,
    var color: String = "",
    var label: String = "",
    var spannableString : String = "",
    var isBold : Boolean = false,
    var isItalic : Boolean = false,
    var isUnderline : Boolean = false,
    var isStrikethrough : Boolean = false,
    var textSize : Float = 18f,
    var foregroundColorText : String = "",
    var backgroundColorText : String = "",
)