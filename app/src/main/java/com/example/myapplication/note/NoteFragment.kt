package com.example.myapplication.note

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
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
            changeToCreateNoteFragment()
        }
        setUpRecyclerView()
        getData()
        searchNote()
        return viewBinding.root
    }

    private fun searchNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.getFilter().filter(p0.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {
                TODO("Not yet implemented")
            }
        })

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
        adapter = AdapterRecyclerViewNote(listNote, object : InterfaceOnClickListener{
            override fun onClickItemNoteListener(note: Note) {
                changeToDetailNoteFragment(note)
            }
        }, requireContext())
        viewBinding.recyclerViewNote.layoutManager = linearLayout
        viewBinding.recyclerViewNote.adapter = adapter
    }

    private fun changeToCreateNoteFragment(){
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
    private fun changeToDetailNoteFragment(note : Note){
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("Note", note)
        detailFragment.arguments = bundle
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
}