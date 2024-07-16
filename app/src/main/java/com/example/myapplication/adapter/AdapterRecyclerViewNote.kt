package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.CustomItemRecyclerViewBinding
import com.example.myapplication.model.Note

class AdapterRecyclerViewNote(private var listNote : ArrayList<Note>) : RecyclerView.Adapter<ViewHolderNote>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNote {
        val viewBinding = CustomItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderNote(viewBinding)
    }

    override fun getItemCount(): Int {
        return listNote.size
    }

    override fun onBindViewHolder(holder: ViewHolderNote, position: Int) {
        holder.bindData(listNote[position])
    }

}
class ViewHolderNote(private var viewBinding : CustomItemRecyclerViewBinding) : RecyclerView.ViewHolder(viewBinding.root){
    @SuppressLint("SetTextI18n")
    fun bindData(note : Note){
        if(note.getTitle() == "" && note.getContent() == ""){
            viewBinding.title.text = "Untitled"
        }
        else if(note.getTitle()== "" && note.getContent() != ""){
            viewBinding.title.text = note.getContent()
        }
        else {
            viewBinding.title.text = note.getTitle()
        }
        viewBinding.dayEdited.text = viewBinding.dayEdited.text.toString() + note.getLastTimeEdited()
    }
}