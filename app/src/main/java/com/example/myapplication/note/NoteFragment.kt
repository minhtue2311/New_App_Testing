package com.example.myapplication.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.LayoutNoteFragmentBinding

class NoteFragment : Fragment() {
    private lateinit var viewBinding : LayoutNoteFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutNoteFragmentBinding.inflate(inflater, container, false)
        viewBinding.searchButton.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.GONE
            viewBinding.layoutSearchToolBar.visibility = View.VISIBLE
        }
        viewBinding.back.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.VISIBLE
            viewBinding.layoutSearchToolBar.visibility = View.GONE
        }
        viewBinding.btnOpenClose.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(viewBinding.navView)
        }
        viewBinding.btnCreate.setOnClickListener{

        }
        return viewBinding.root
    }
    fun changeToDetailFragment(){
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
    }
}