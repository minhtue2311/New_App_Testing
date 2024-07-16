package com.example.myapplication.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.LayoutNoteFragmentBinding

class DetailNoteFragment : Fragment() {
    private lateinit var viewBinding : LayoutNoteFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutNoteFragmentBinding.inflate(inflater, container, false)
        viewBinding.back.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        return viewBinding.root
    }
}