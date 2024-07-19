package com.example.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CustomItemRecyclerViewBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import java.util.Locale

class AdapterRecyclerViewNote(private var listNote : ArrayList<Note>, private var onClickListener : InterfaceOnClickListener, private val context: Context) : RecyclerView.Adapter<ViewHolderNote>() {
    private var listNoteBackUp = listNote
    private var listNoteSelected : ArrayList<Note> = ArrayList()
    var isSelected = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNote {
        val viewBinding = CustomItemRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderNote(viewBinding)
    }

    override fun getItemCount(): Int {
        return listNote.size
    }

    override fun onBindViewHolder(holder: ViewHolderNote, position: Int) {
        val model = listNote[position]
        holder.bindData(model, context, listNoteSelected)
        holder.getViewBinding().cardViewItem.setOnClickListener {
            if(!isSelected){
                onClickListener.onClickItemNoteListener(model)
            }
            else{
                if(listNoteSelected.contains(model)){
                    if(model.color != ""){
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(R.drawable.border_cardview_background, Color.parseColor(model.color))
                    }else {
                        holder.getViewBinding().linearLayoutItem.setBackgroundResource(R.drawable.border_cardview_background)
                    }
                    listNoteSelected.remove(model)
                }else{
                    if(model.color != ""){
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, Color.parseColor(model.color))
                    }else {
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, makeColorDarker(context.resources.getColor(R.color.colorItem)))
                    }
                    listNoteSelected.add(model)
                }
                callBack()
                if(listNoteSelected.size ==0){
                    isSelected = false
                }
            }
        }
        holder.getViewBinding().cardViewItem.setOnLongClickListener {
            if(!isSelected){
                isSelected = true
                if(model.color != ""){
                    holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, Color.parseColor(model.color))
                }else {
                    holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, makeColorDarker(context.resources.getColor(R.color.colorItem)))
                }
                listNoteSelected.add(model)
                callBack()
            }
            true
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun onAllSelected(){
        isSelected = true
        listNoteSelected.clear()
        listNoteSelected.addAll(listNote)
        notifyDataSetChanged()
        callBack()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun onAllUnSelected(){
        listNoteSelected.clear()
        notifyDataSetChanged()
        isSelected = false
        callBack()
    }
    fun clearListSelectedNote(){
        listNoteSelected.clear()
    }
    private fun callBack(){
        onClickListener.onSelectedNote(listNoteSelected)
    }
    private fun handleDrawableColor(drawableInput: Int, colorInt : Int): GradientDrawable {
        val drawable = ContextCompat.getDrawable(context, drawableInput)
            ?.mutate() as GradientDrawable  //make clone of drawable for not changing the original drawable
        drawable.setColor(colorInt)
        return drawable
    }
    private fun makeColorDarker(color : Int) : Int{
        val hsv = FloatArray(3)
        Color.colorToHSV(color,hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
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
    fun bindData(note : Note, context : Context, listNoteSelected : ArrayList<Note>){
       viewBinding.title.text = note.label
        viewBinding.dayEdited.text = context.getString(R.string.last_edited)  + note.editedDate
        if(listNoteSelected.contains(note)){
            if(note.color != ""){
                viewBinding.linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, Color.parseColor(note.color), context)
            }else {
                viewBinding.linearLayoutItem.background = handleDrawableColor(R.drawable.border_item_selected, makeColorDarker(context.resources.getColor(R.color.colorItem)), context)
            }
        }else {
            if(note.color != ""){
                viewBinding.linearLayoutItem.background = handleDrawableColor(R.drawable.border_cardview_background, Color.parseColor(note.color), context)
            }else {
                viewBinding.linearLayoutItem.setBackgroundResource(R.drawable.border_cardview_background)
            }
        }
    }
    fun getViewBinding() : CustomItemRecyclerViewBinding{
        return viewBinding
    }
    private fun handleDrawableColor(drawableInput: Int, colorInt : Int, context: Context): GradientDrawable {
        val drawable = ContextCompat.getDrawable(context, drawableInput)
            ?.mutate() as GradientDrawable  //make clone of drawable for not changing the original drawable
        drawable.setColor(colorInt)
        return drawable
    }
    private fun makeColorDarker(color : Int) : Int{
        val hsv = FloatArray(3)
        Color.colorToHSV(color,hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
    }
}