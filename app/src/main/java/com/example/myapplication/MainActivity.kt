package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.note.NoteFragment

class MainActivity : AppCompatActivity()  {
    private lateinit var viewBinding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        replaceFragment()
    }
    private fun replaceFragment(){
        val homeFragment = NoteFragment()
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, homeFragment)
        fragmentTrans.commit()
    }
}