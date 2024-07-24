package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Note")
class Note(
    var title: String,
    var content: String,
    var createDate: String,
    var editedDate: String
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var idNote: Int? = null
    var color: String = ""
    var label: String = ""
    var listCategories : String = ""
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
        idNote = parcel.readValue(Int::class.java.classLoader) as? Int
        color = parcel.readString() ?: ""
        label = parcel.readString() ?: ""
        listCategories = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(createDate)
        parcel.writeString(editedDate)
        parcel.writeValue(idNote)
        parcel.writeString(color)
        parcel.writeString(label)
        parcel.writeString(listCategories)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}
