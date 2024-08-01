package com.example.myapplication.menu_bar

import android.content.res.Resources
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import androidx.viewbinding.ViewBindings
import com.example.myapplication.R
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.note.NoteFragment
import com.google.android.material.navigation.NavigationView

class MenuBarFunction {
    fun loadCategoriesToMenu(listCategories: List<Categories>, menu: Menu, resources: Resources) {
        val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)

        if (categoriesGroupItem != null) {
            val categoriesGroup = categoriesGroupItem.subMenu
            categoriesGroup?.let {
                it.clear()
                //Add Edit Categories Item Menu
                it.add(
                    Menu.NONE,
                    R.id.editCategories,
                    Menu.NONE,
                    resources.getString(R.string.edit_categorized)
                )
                    .setIcon(R.drawable.baseline_playlist_add_24)
                //Add Uncategorized Item Menu
                it.add(Menu.NONE, 2311, Menu.NONE, resources.getString(R.string.uncategorized))
                    .setIcon(R.drawable.dont_tag)
                listCategories.forEachIndexed { index, category ->
                    val itemId = Menu.FIRST + index
                    it.add(R.id.categoriesGroup, itemId, Menu.NONE, category.nameCategories)
                        ?.setIcon(R.drawable.tag)
                }
            }
        }
    }
}