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

class AdapterRecyclerViewNote(
    private var listNote: ArrayList<Note>,
    private var onClickListener: InterfaceOnClickListener,
    private val context: Context
) : RecyclerView.Adapter<ViewHolderNote>() {
    private var listNoteBackUp = listNote
    private var listNoteSelected: ArrayList<Note> = ArrayList()
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
        holder.bindData(model, context, listNoteSelected)
        holder.getViewBinding().cardViewItem.setOnClickListener {
            if (!isSelected) {
                onClickListener.onClickItemNoteListener(model)
            } else {
                if (listNoteSelected.contains(model)) {
                    if (model.color != "") {
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                            R.drawable.border_cardview_background,
                            Color.parseColor(model.color),
                            false
                        )
                    } else {
                        holder.getViewBinding().linearLayoutItem.setBackgroundResource(R.drawable.border_cardview_background)
                    }
                    listNoteSelected.remove(model)
                } else {
                    if (model.color != "") {
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                            R.drawable.border_item_selected,
                            Color.parseColor(model.color),
                            true
                        )
                    } else {
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                            R.drawable.border_item_selected,
                            context.resources.getColor(R.color.colorItem),
                            true
                        )
                    }
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
                if (model.color != "") {
                    holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                        R.drawable.border_item_selected,
                        Color.parseColor(model.color),
                        true
                    )
                } else {
                    holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                        R.drawable.border_item_selected,
                        context.resources.getColor(R.color.colorItem),
                        true
                    )
                }
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

    private fun handleDrawableColor(
        drawableInput: Int,
        colorInt: Int,
        isSelectedItem: Boolean
    ): GradientDrawable {
        val drawable = ContextCompat.getDrawable(context, drawableInput)
            ?.mutate() as GradientDrawable
        //make clone of drawable for not changing the original drawable
        var chosenColor = lightenColor(colorInt, 0.8f)
        val gradientDrawable = GradientDrawable()
        gradientDrawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
        if (isSelectedItem) {
            chosenColor = darkenColor(colorInt, 0.3f)
            gradientDrawable.colors = intArrayOf(colorInt, chosenColor)
        } else {
            gradientDrawable.colors = intArrayOf(chosenColor, colorInt)
        }
        gradientDrawable.shape = drawable.shape
        gradientDrawable.cornerRadius = drawable.cornerRadius
        var strokeWidth = 4
        var strokeColor = Color.parseColor("#927855")
        if (isSelectedItem && colorInt != Color.parseColor("#ffffdd")) {
            val dashWidth = 80f
            val dashGap = 20f
            strokeColor = darkenColor(colorInt, 0.2f)
            strokeWidth = 6
            gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        } else if (isSelectedItem && colorInt == Color.parseColor("#ffffdd")) {
            strokeColor = Color.parseColor("#a9926d")
            val dashWidth = 80f
            val dashGap = 20f
            strokeWidth = 6
            gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        } else if (!isSelectedItem) {
            gradientDrawable.setStroke(strokeWidth, strokeColor)
        }
        return gradientDrawable
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = Color.alpha(color)

        val newR = (r + ((255 - r) * factor)).toInt()
        val newG = (g + ((255 - g) * factor)).toInt()
        val newB = (b + ((255 - b) * factor)).toInt()

        return Color.argb(alpha, newR, newG, newB)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = Color.alpha(color)

        val newR = (r * (1 - factor)).toInt()
        val newG = (g * (1 - factor)).toInt()
        val newB = (b * (1 - factor)).toInt()

        return Color.argb(alpha, newR, newG, newB)
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
    fun bindData(note: Note, context: Context, listNoteSelected: ArrayList<Note>) {
        viewBinding.title.text = note.label
        viewBinding.dayEdited.text = context.getString(R.string.last_edited) + note.editedDate
        viewBinding.categories.text = handleListCategories(note.listCategories)
        if (listNoteSelected.contains(note)) {
            if (note.color != "") {
                viewBinding.linearLayoutItem.background = handleDrawableColor(
                    R.drawable.border_item_selected,
                    Color.parseColor(note.color),
                    context,
                    true
                )
            } else {
                viewBinding.linearLayoutItem.background = handleDrawableColor(
                    R.drawable.border_item_selected,
                    context.resources.getColor(R.color.colorItem),
                    context,
                    true
                )
            }
        } else {
            if (note.color != "") {
                viewBinding.linearLayoutItem.background = handleDrawableColor(
                    R.drawable.border_cardview_background,
                    Color.parseColor(note.color),
                    context,
                    false
                )
            } else {
                viewBinding.linearLayoutItem.setBackgroundResource(R.drawable.border_cardview_background)
            }
        }
    }

    fun getViewBinding(): CustomItemRecyclerViewBinding {
        return viewBinding
    }

    private fun handleDrawableColor(
        drawableInput: Int,
        colorInt: Int,
        context: Context,
        isSelected: Boolean
    ): GradientDrawable {
        val drawable = ContextCompat.getDrawable(context, drawableInput)
            ?.mutate() as GradientDrawable
        //make clone of drawable for not changing the original drawable
        var chosenColor = lightenColor(colorInt, 0.7f)
        val gradientDrawable = GradientDrawable()
        gradientDrawable.orientation = GradientDrawable.Orientation.TOP_BOTTOM
        if (isSelected) {
            chosenColor = darkenColor(colorInt, 0.2f)
            gradientDrawable.colors = intArrayOf(colorInt, chosenColor)
        } else {
            gradientDrawable.colors = intArrayOf(chosenColor, colorInt)
        }
        gradientDrawable.shape = drawable.shape
        gradientDrawable.cornerRadius = drawable.cornerRadius
        var strokeWidth = 4
        var strokeColor = Color.parseColor("#927855")
        if (isSelected && colorInt != Color.parseColor("#ffffdd")) {
            val dashWidth = 80f
            val dashGap = 20f
            strokeColor = darkenColor(colorInt, 0.2f)
            strokeWidth = 6
            gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        } else if (isSelected && colorInt == Color.parseColor("#ffffdd")) {
            strokeColor = Color.parseColor("#a9926d")
            val dashWidth = 80f
            val dashGap = 10f
            strokeWidth = 6
            gradientDrawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
        } else if (!isSelected) {
            gradientDrawable.setStroke(strokeWidth, strokeColor)
        }
        return gradientDrawable
    }

    private fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = Color.alpha(color)

        val newR = (r + ((255 - r) * factor)).toInt()
        val newG = (g + ((255 - g) * factor)).toInt()
        val newB = (b + ((255 - b) * factor)).toInt()

        return Color.argb(alpha, newR, newG, newB)
    }

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val alpha = Color.alpha(color)

        val newR = (r * (1 - factor)).toInt()
        val newG = (g * (1 - factor)).toInt()
        val newB = (b * (1 - factor)).toInt()

        return Color.argb(alpha, newR, newG, newB)
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