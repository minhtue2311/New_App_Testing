package com.example.myapplication.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Note")
class Note(title: String, content: String, lastTimeEdited : String) : java.io.Serializable {
    @PrimaryKey(autoGenerate = true)
    private var id: Int? = 0
    private var title : String? = title
    private var content : String? = content
    private var color : String = ""
    private var lastTimeEdited : String? = lastTimeEdited
    fun getId(): Int {
        return id!!
    }

    fun setId(id: Int) {
        this.id = id
    }
    fun getTitle() : String{
        return title!!
    }
    fun getContent() : String{
        return content!!
    }
    fun setTitle(title : String){
        this.title = title
    }
    fun setContent(content : String){
        this.content = content
    }
    fun setColor(color : String){
        this.color = color
    }
    fun getColor(): String{
        return color
    }
    fun setDay(time : String){
        this.lastTimeEdited = time
    }
    fun getLastTimeEdited() : String{
        return lastTimeEdited!!
    }
}