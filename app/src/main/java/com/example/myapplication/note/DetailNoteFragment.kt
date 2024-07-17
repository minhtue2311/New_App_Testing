package com.example.myapplication.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.LayoutDetailNoteBinding
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DetailNoteFragment : Fragment() {
    private lateinit var viewBinding : LayoutDetailNoteBinding
    private var note : Note? = null
    private var type : String = "Create"
    private lateinit var noteDatabase : NoteDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutDetailNoteBinding.inflate(inflater, container, false)
        getData()
        noteDatabase = NoteDatabase.getInstance(requireContext())
        viewBinding.scrollViewLayout.setOnClickListener {
            //TO DO
        }
        viewBinding.back.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        viewBinding.txtSave.setOnClickListener {
            when(type){
                "Create" -> {
                    var title = ""
                    var content = ""
                    if(viewBinding.editTextTitle.text.isNotEmpty()){
                        title = viewBinding.editTextTitle.text.toString()
                    }
                    if(viewBinding.editTextContent.text.isNotEmpty()){
                        content = viewBinding.editTextContent.text.toString()
                    }
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateString = sdf.format(Date())
                    val noteModel = Note(title,content, dateString)
                    noteDatabase.noteDao().insertNote(noteModel)
                    Toast.makeText(requireContext(), "Save", Toast.LENGTH_SHORT).show()
                }
                "Update" -> {
                    val titleValue = viewBinding.editTextTitle.text.toString()
                    val contentValue = viewBinding.editTextContent.text.toString()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateString = sdf.format(Date())
                    note?.apply {
                        title = titleValue
                        content = contentValue
                        lastTimeEdited = dateString
                    }
                    note?.let { noteDatabase.noteDao().updateNote(it) }
                }
            }
        }
        return viewBinding.root
    }
    private fun getData(){
        val bundle = arguments
        if(bundle != null) {
            note = bundle.getParcelable<Note>("Note")
            bindData()
            type = "Update"
        }
    }
    private fun bindData(){
        viewBinding.editTextTitle.setText(note?.title.toString())
        viewBinding.editTextContent.setText(note?.content.toString())
    }
}