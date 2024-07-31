package com.example.myapplication.categories.categories_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Categories

import com.example.myapplication.repository.CategoriesRepository


class CategoriesViewModel : ViewModel() {
    private lateinit var categoriesRepository: CategoriesRepository
    private lateinit var liveDataCategories: LiveData<List<Categories>>

    fun getLiveDataCategories(context: Context): LiveData<List<Categories>> {
        categoriesRepository = CategoriesRepository()
        liveDataCategories = categoriesRepository.getAllCategories(context)
        return liveDataCategories
    }
}