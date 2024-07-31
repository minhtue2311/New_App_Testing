package com.example.myapplication.preferences

import android.content.Context
import android.content.SharedPreferences

class NoteStatusPreferences(private val context: Context) {
    private val myPreferences : String = "NotePreferences"
    fun putStatusSortValues(status : String){
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences!!.edit()
        editor.putString("statusSort", status)
        editor.apply()
    }
    fun getStatusSortValues() : String? {
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        return sharedPreferences!!.getString("statusSort", null)
    }
    fun putStatusFormattingBar(status : Boolean){
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences!!.edit()
        editor.putBoolean("statusFormattingBar", status)
        editor.apply()
    }
    fun getStatusFormattingBar() : Boolean {
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        return sharedPreferences!!.getBoolean("statusFormattingBar", false)
    }
    fun putStatusTrashValues(status : Boolean){
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences!!.edit()
        editor.putBoolean("statusTrash", status)
        editor.apply()
    }
    fun getStatusTrashValues() : Boolean{
        val sharedPreferences : SharedPreferences? = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        return sharedPreferences!!.getBoolean("statusTrash", false)
    }
}