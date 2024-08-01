package com.example.myapplication.menu_bar

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.R
import com.example.myapplication.categories.CategoriesFragment
import com.example.myapplication.model.Categories
import com.example.myapplication.note.NoteFragment
import com.example.myapplication.setting.SettingFragment
import com.example.myapplication.trash.TrashFragment

class ChangeFragmentFunctions(private var activity: FragmentActivity) {
    fun onChangeToNoteFragmentWithCategories(value: Categories) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putSerializable("category", value)
        bundle.putString("Type", "category")
        noteFragment.arguments = bundle
        val fragmentTrans = activity.supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }

    fun onChangeToNoteFragment(value: String) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putString("Type", value)
        noteFragment.arguments = bundle
        val fragmentTrans = activity.supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }

    fun onChangedToCategoriesFragment() {
        val categoriesFragment = CategoriesFragment()
        val fragmentTrans = activity.supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, categoriesFragment)
        fragmentTrans.commit()
    }

    fun onChangeToSettingFragment() {
        val settingFragment = SettingFragment()
        val fragmentTrans = activity.supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, settingFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }

    fun onChangeToTrashFragment() {
        val trashFragment = TrashFragment()
        val fragmentTrans = activity.supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, trashFragment)
        fragmentTrans.commit()
    }
}