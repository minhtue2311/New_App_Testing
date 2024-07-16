package com.example.myapplication.note

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterRecyclerViewNote
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.model.Note
import com.example.myapplication.note.noteViewModel.NoteViewModel

class NoteFragment : Fragment() {
    private lateinit var viewBinding : LayoutNoteFragmentBinding
    private lateinit var listNote : ArrayList<Note>
    private lateinit var adapter : AdapterRecyclerViewNote
    private lateinit var viewModel : NoteViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutNoteFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
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
            changeToDetailFragment()
        }
        setUpRecyclerView()
        getData()
        return viewBinding.root
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getData(){
        viewModel.getLiveDataNote(requireContext()).observe(viewLifecycleOwner){
            if(listNote.isNotEmpty()){
                listNote.clear()
                for(note in it){
                    listNote.add(note)
                }
            }
            else{
                for(note in it){
                    listNote.add(note)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    private fun setUpRecyclerView() {
        viewBinding.recyclerViewNote.setHasFixedSize(false)
        listNote = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        adapter = AdapterRecyclerViewNote(listNote)
        viewBinding.recyclerViewNote.layoutManager = linearLayout
        viewBinding.recyclerViewNote.adapter = adapter
    }

    private fun changeToDetailFragment(){
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
}