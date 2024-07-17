package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CustomItemRecyclerViewBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import java.util.Locale

class AdapterRecyclerViewNote(private var listNote : ArrayList<Note>, private var onClickListener : InterfaceOnClickListener, private val context: Context) : RecyclerView.Adapter<ViewHolderNote>() {
    private var listNoteBackUp = listNote
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNote {
        val viewBinding = CustomItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderNote(viewBinding)
    }

    override fun getItemCount(): Int {
        return listNote.size
    }

    override fun onBindViewHolder(holder: ViewHolderNote, position: Int) {
        val model = listNote[position]
        holder.bindData(model, context)
        holder.getViewBinding().cardViewItem.setOnClickListener {
            onClickListener.onClickItemNoteListener(model)
        }
    }
    fun getFilter() : Filter {
        val f = object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val fr = FilterResults()
                if(p0 == null || p0.isEmpty()){
                    fr.count = listNoteBackUp.size
                    fr.values = listNoteBackUp
                }
                else{
                    val newData = ArrayList<Note>()
                    for(note in listNoteBackUp){
                        if(note.label.lowercase(Locale.ROOT).contains(p0.toString()
                                .lowercase(Locale.ROOT)))
                        {
                            newData.add(note)
                        }
                    }
                    fr.count = newData.size
                    fr.values = newData
                }
                return fr
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                listNote = ArrayList()
                val temp : ArrayList<Note> = p1!!.values as ArrayList<Note>
                for(note in temp){
                    listNote.add(note)
                }
                notifyDataSetChanged()
            }

        }
        return f
    }
}
class ViewHolderNote(private var viewBinding : CustomItemRecyclerViewBinding) : RecyclerView.ViewHolder(viewBinding.root){
    @SuppressLint("SetTextI18n")
    fun bindData(note : Note, context : Context){
        if(note.title == "" && note.content == ""){
            viewBinding.title.text = "Untitled"
            note.label = "Untitled"
        }
        else if(note.title == "" && note.content != ""){
            viewBinding.title.text = note.content
            note.label = note.content
        }
        else {
            viewBinding.title.text = note.title
            note.label = note.title
        }
        viewBinding.dayEdited.text = context.getString(R.string.last_edited)  + note.lastTimeEdited
    }
    fun getViewBinding() : CustomItemRecyclerViewBinding{
        return viewBinding
    }
}