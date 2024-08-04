package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CustomItemRecyclerViewBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.options.HandleColorItemRecyclerVIew
import java.util.Locale

class AdapterRecyclerViewNote(
    private var listNote: ArrayList<Note>,
    private var onClickListener: InterfaceOnClickListener,
    private val context: Context
) : RecyclerView.Adapter<ViewHolderNote>() {
    private var listNoteBackUp = listNote
    private var listNoteSelected: ArrayList<Note> = ArrayList()
    private var handleColorItemRecyclerVIew = HandleColorItemRecyclerVIew(context)
    var isSelected = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNote {
        val viewBinding = CustomItemRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolderNote(viewBinding)
    }

    override fun getItemCount(): Int {
        return listNote.size
    }
    override fun onBindViewHolder(holder: ViewHolderNote, position: Int) {
        val model = listNote[position]
        holder.bindData(model, context, listNoteSelected, handleColorItemRecyclerVIew)
        holder.getViewBinding().cardViewItem.setOnClickListener {
            if (!isSelected) {
                onClickListener.onClickItemNoteListener(model)
            } else {
                if (listNoteSelected.contains(model)) {
                    handleColorItemRecyclerVIew.changeBackgroundItem(holder.getViewBinding().linearLayoutItem, model,false)
                    listNoteSelected.remove(model)
                } else {
                    handleColorItemRecyclerVIew.changeBackgroundItem(holder.getViewBinding().linearLayoutItem,model, true)
                    listNoteSelected.add(model)
                }
                callBack()
                if (listNoteSelected.size == 0) {
                    isSelected = false
                }
            }
        }
        holder.getViewBinding().cardViewItem.setOnLongClickListener {
            if (!isSelected) {
                isSelected = true
                handleColorItemRecyclerVIew.changeBackgroundItem(holder.getViewBinding().linearLayoutItem, model, true)
                listNoteSelected.add(model)
                callBack()
            }
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onAllSelected() {
        isSelected = true
        listNoteSelected.clear()
        listNoteSelected.addAll(listNote)
        notifyDataSetChanged()
        callBack()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onAllUnSelected() {
        listNoteSelected.clear()
        notifyDataSetChanged()
        isSelected = false
        callBack()
    }

    fun clearListSelectedNote() {
        listNoteSelected.clear()
    }

    private fun callBack() {
        onClickListener.onSelectedNote(listNoteSelected)
    }
    fun getFilter(): Filter {
        val f = object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val fr = FilterResults()
                if (p0.isNullOrEmpty()) {
                    fr.count = listNoteBackUp.size
                    fr.values = listNoteBackUp
                } else {
                    val newData = ArrayList<Note>()
                    for (note in listNoteBackUp) {
                        if (note.label.lowercase(Locale.ROOT).contains(
                                p0.toString()
                                    .lowercase(Locale.ROOT)
                            )
                        ) {
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
                val temp: ArrayList<Note> = p1!!.values as ArrayList<Note>
                for (note in temp) {
                    listNote.add(note)
                }
                notifyDataSetChanged()
            }

        }
        return f
    }
}

class ViewHolderNote(private var viewBinding: CustomItemRecyclerViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    @SuppressLint("SetTextI18n")
    fun bindData(note: Note, context: Context, listNoteSelected: ArrayList<Note>, handleColorItemRecyclerVIew: HandleColorItemRecyclerVIew) {
        viewBinding.title.text = note.label
        viewBinding.dayEdited.text = context.getString(R.string.last_edited) + note.editedDate
        if(!note.isDelete){
            viewBinding.categories.text = handleListCategories(note.listCategories)
        }
        if (listNoteSelected.contains(note)) {
            handleColorItemRecyclerVIew.changeBackgroundItem(viewBinding.linearLayoutItem, note, true)
        } else {
           handleColorItemRecyclerVIew.changeBackgroundItem(viewBinding.linearLayoutItem, note, false)
        }
    }
    fun getViewBinding(): CustomItemRecyclerViewBinding {
        return viewBinding
    }
    private fun handleListCategories(categories : String) : String{
        var stringResult = ""
        if(categories.length <= 20){
            stringResult = categories
        }else{
            for(character in categories){
                if(stringResult.length <= 20){
                    stringResult += character
                }else{
                    stringResult += "...."
                    break
                }
            }
        }
        return stringResult
    }
}