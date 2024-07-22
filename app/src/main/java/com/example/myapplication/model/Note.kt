package com.example.myapplication.model


import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Note",
        foreignKeys = [ForeignKey(entity = Categories::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId"),
            onDelete = ForeignKey.CASCADE)])
class Note(
    var title: String,
    var content: String,
    var createDate: String,
    var editedDate : String,
) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var color: String = ""
    var label : String = ""
    var categoryId : Int? = null

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",

    ) {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        color = parcel.readString() ?: ""
        label = parcel.readString() ?: ""
        categoryId = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(createDate)
        parcel.writeString(editedDate)
        parcel.writeValue(id)
        parcel.writeString(color)
        parcel.writeString(label)
        parcel.writeValue(categoryId)
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