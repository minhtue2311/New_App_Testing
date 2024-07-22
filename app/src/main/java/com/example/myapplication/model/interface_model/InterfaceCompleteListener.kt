package com.example.myapplication.model.interface_model

import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note


interface InterfaceCompleteListener {
    fun onCompleteListener(note : Note)
    fun onEditCategoriesListener(categories: Categories)
    fun onDeleteCategoriesListener(categories: Categories)
}