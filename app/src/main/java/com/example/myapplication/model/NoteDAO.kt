package com.example.myapplication.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.model.relation.CategoryWithNotes
import com.example.myapplication.model.relation.NoteCategoryRef
import com.example.myapplication.model.relation.NoteWithCategories

@Dao
interface NoteDAO {
    @Insert
    fun insertNote(note: Note)
    @Insert
    fun insertNoteAndGetId(note: Note): Long


    @Update
    fun updateNote(note: Note)

    @Query("SELECT * FROM Note")
    fun getAllNote() : LiveData<List<Note>>

    @Query("SELECT * FROM Note WHERE idNote NOT IN (SELECT DISTINCT idNote FROM NoteCategoryRef)")
    fun getNotesWithoutCategories(): LiveData<List<Note>>

    @Query("DELETE FROM NoteCategoryRef WHERE idNote = :noteId")
    fun deleteNoteCategoriesRefByNoteId(noteId: Int)


    @Delete
    fun delete(note: Note)

    @Insert
    fun insertNoteCategoryCrossRef(crossRef: NoteCategoryRef)

    @Delete
    fun deleteNoteCategoryCrossRef(crossRef: NoteCategoryRef)

    @Transaction
    @Query("SELECT * FROM Note WHERE idNote = :noteId")
    fun getNoteWithCategories(noteId: Int): LiveData<NoteWithCategories>

    @Transaction
    @Query("SELECT * FROM Categories WHERE idCategory = :categoryId")
    fun getCategoryWithNotes(categoryId: Int): LiveData<CategoryWithNotes>

    @Query("SELECT COUNT(*) FROM NoteCategoryRef WHERE idNote = :noteId AND idCategory = :categoryId")
    fun checkNoteCategoryRefExists(noteId: Int, categoryId: Int): Int

    @Insert
    fun moveNoteToTrash(trash: Trash)


    @Query("SELECT * FROM Trash")
    fun getAllNoteFromTrash() : LiveData<List<Trash>>

    @Delete
    fun deleteTrash(trash: Trash)

}

@Dao
interface CategoriesDAO{
    @Insert
    fun insertCategories(categories : Categories)

    @Update
    fun updateCategories(categories : Categories)

    @Query("SELECT * FROM Categories")
    fun getAllCategories() : LiveData<List<Categories>>

    @Delete
    fun deleteCategory(categories : Categories)

    @Query("DELETE FROM NoteCategoryRef WHERE idCategory = :categoryId")
    fun deleteNoteCategoriesByCategoryId(categoryId: Int)
}