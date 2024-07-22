package com.example.myapplication.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.myapplication.model.Categories
import com.example.myapplication.model.NoteDatabase

class CategoriesRepository {
    fun getAllCategories(context : Context) : LiveData<List<Categories>> {
        return NoteDatabase.getInstance(context).categoriesDao().getAllCategories()
    }
}