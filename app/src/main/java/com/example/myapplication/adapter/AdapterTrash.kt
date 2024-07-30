package com.example.myapplication.adapter

import com.example.myapplication.model.Trash


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CustomItemRecyclerViewBinding

class AdapterTrash(
    private var listTrash: ArrayList<Trash>,
    private var onClickListener: OnClickTrashListener,
    private val context: Context
) : RecyclerView.Adapter<ViewHolderTrash>() {
    private var listTrashSelected: ArrayList<Trash> = ArrayList()
    var isSelected = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTrash {
        val viewBinding = CustomItemRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolderTrash(viewBinding)
    }

    override fun getItemCount(): Int {
        return listTrash.size
    }
    override fun onBindViewHolder(holder: ViewHolderTrash, position: Int) {
        val model = listTrash[position]
        holder.bindData(model, context, listTrashSelected)
        holder.getViewBinding().cardViewItem.setOnClickListener {
            if (!isSelected) {
                onClickListener.onClickTrashListener(model)
            } else {
                if (listTrashSelected.contains(model)) {
                    if (model.color != "") {
                        holder.getViewBinding().linearLayoutItem.background = handleDrawableColor(
                            R.drawable.border_cardview_background,
                            Color.parseColor(model.color),
                            false
                        )
                    } else {
                        holder.getViewBinding().linearLayoutItem.setBackgroundResource(R.drawable.border_cardview_background)
                    }
                    listTrashSelected.remove(model)
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
                    listTrashSelected.add(model)
                }
                callBack()
                if (listTrashSelected.size == 0) {
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
                listTrashSelected.add(model)
                callBack()
            }
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onAllSelected() {
        isSelected = true
        listTrashSelected.clear()
        listTrashSelected.addAll(listTrash)
        notifyDataSetChanged()
        callBack()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onAllUnSelected() {
        listTrashSelected.clear()
        notifyDataSetChanged()
        isSelected = false
        callBack()
    }

    fun clearListSelectedNote() {
        listTrashSelected.clear()
    }

    private fun callBack() {
        onClickListener.onLongClickTrashListener(listTrashSelected)
    }

    interface OnClickTrashListener{
        fun onClickTrashListener(trash: Trash)
        fun onLongClickTrashListener(listTrash : ArrayList<Trash>)
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
}

class ViewHolderTrash(private var viewBinding: CustomItemRecyclerViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    @SuppressLint("SetTextI18n")
    fun bindData(trash: Trash, context: Context, listNoteSelected: ArrayList<Trash>) {
        viewBinding.title.text = trash.label
        viewBinding.dayEdited.text = context.getString(R.string.last_edited) + trash.editedDate
        if (listNoteSelected.contains(trash)) {
            if (trash.color != "") {
                viewBinding.linearLayoutItem.background = handleDrawableColor(
                    R.drawable.border_item_selected,
                    Color.parseColor(trash.color),
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
            if (trash.color != "") {
                viewBinding.linearLayoutItem.background = handleDrawableColor(
                    R.drawable.border_cardview_background,
                    Color.parseColor(trash.color),
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
}