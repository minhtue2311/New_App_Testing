package com.example.myapplication.note

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterRecyclerViewNote
import com.example.myapplication.databinding.CustomLayoutDialogSortBinding
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.noteViewModel.NoteViewModel
import com.example.myapplication.preferences.NoteStatusPreferences
import java.util.Stack

@SuppressLint("NotifyDataSetChanged")
class NoteFragment : Fragment() {
    private lateinit var viewBinding : LayoutNoteFragmentBinding
    private lateinit var listNote : ArrayList<Note>
    private lateinit var adapter : AdapterRecyclerViewNote
    private lateinit var viewModel : NoteViewModel
    private lateinit var preferences : NoteStatusPreferences
    private var statusSort : String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutNoteFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        preferences = NoteStatusPreferences(requireContext())
        statusSort = preferences.getStatusSortValues()
        viewBinding.searchButton.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.GONE
            viewBinding.layoutSearchToolBar.visibility = View.VISIBLE
        }
        viewBinding.back.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.VISIBLE
            viewBinding.layoutSearchToolBar.visibility = View.GONE
            viewBinding.searchBar.text.clear()
        }
        viewBinding.btnOpenClose.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(viewBinding.navView)
        }
        viewBinding.btnCreate.setOnClickListener{
            changeToCreateNoteFragment()
        }
        viewBinding.txtSort.setOnClickListener {
            openDialogForSorting()
        }
        setUpRecyclerView()
        getData()
        searchNote()
        return viewBinding.root
    }
    private fun checkStatusSort() {
        if(statusSort != null){
            when(statusSort){
                "A->Z" -> {
                    sortByAToZ()
                }
                "Z->A" -> {
                    sortByZtoA()
                }
                "NewestDateCreated" -> {sortByNewestDate()}
                "OldestDateCreated" -> {sortByOldestDate()}
                "NewestDateEdited" -> {sortByNewestEditDay()}
                "OldestDateEdited" ->{sortByOldestEditDay()}
            }
        }
    }

    private fun searchNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.getFilter().filter(p0.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {

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
            checkStatusSort()
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

            override fun onClickColorItem(color: String) {

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
    private fun openDialogForSorting(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = CustomLayoutDialogSortBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.show()
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
        val status = preferences.getStatusSortValues()
        when(status){
            "A->Z" -> {binding.rbTitleAtoZ.isChecked = true}
            "Z->A" -> {binding.rbTitleZtoA.isChecked = true}
            "NewestDateCreated" -> {binding.rbFromNewestDate.isChecked = true}
            "OldestDateCreated" -> {binding.rbFromOldestDate.isChecked = true}
            "NewestDateEdited" -> {binding.rbEditDateNewest.isChecked = true}
            "OldestDateEdited" ->{binding.rbEditDateOldest.isChecked = true}
        }
        binding.btnSort.setOnClickListener {
            if(binding.rbEditDateNewest.isChecked){
                sortByNewestEditDay()
                preferences.putStatusSortValues("NewestDateEdited")
            }else if(binding.rbEditDateOldest.isChecked){
                sortByOldestEditDay()
                preferences.putStatusSortValues("OldestDateEdited")
            }else if(binding.rbTitleAtoZ.isChecked){
                sortByAToZ()
                preferences.putStatusSortValues("A->Z")
            }else if(binding.rbTitleZtoA.isChecked){
                sortByZtoA()
                preferences.putStatusSortValues("Z->A")
            }else if(binding.rbFromNewestDate.isChecked){
                sortByNewestDate()
                preferences.putStatusSortValues("NewestDateCreated")
            }else if(binding.rbFromOldestDate.isChecked){
                sortByOldestDate()
                preferences.putStatusSortValues("OldestDateCreated")
            }
            dialog.cancel()
        }
    }
    private fun sortByOldestDate() {
        listNote.sortWith(Comparator{h1,h2 ->
            h1.createDate.compareTo(h2.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestDate() {
        listNote.sortWith(Comparator{h1,h2 ->
            h2.createDate.compareTo(h1.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByZtoA() {
        listNote.sortWith(Comparator{h1,h2 ->
            h2.label.lowercase().compareTo(h1.label.lowercase())
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByAToZ() {
        listNote.sortWith { h1, h2 ->
            h1.label.lowercase().compareTo(h2.label.lowercase())
        }
        adapter.notifyDataSetChanged()
    }

    private fun sortByOldestEditDay() {
        listNote.sortWith(Comparator{h1,h2 ->
            h1.editedDate.compareTo(h2.editedDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestEditDay() {
        listNote.sortWith(Comparator{h1,h2 ->
            h2.editedDate.compareTo(h1.editedDate)
        })
        adapter.notifyDataSetChanged()
    }
}