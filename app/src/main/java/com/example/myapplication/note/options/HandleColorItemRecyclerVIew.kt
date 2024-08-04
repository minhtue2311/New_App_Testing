package com.example.myapplication.note.options

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.model.Note

class HandleColorItemRecyclerVIew(private val context: Context) {
    fun handleDrawableColor(
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

    fun changeBackgroundItem(linearLayout: LinearLayout, note: Note, isSelected : Boolean) {
        if(isSelected){
            if (note.color != "") {
                linearLayout.background = handleDrawableColor(R.drawable.border_item_selected, Color.parseColor(note.color),true)
            }
            else{
                linearLayout.background = handleDrawableColor(R.drawable.border_item_selected, context.resources.getColor(R.color.colorItem),true)
            }
        }else{
            if(note.color != ""){
                linearLayout.background = handleDrawableColor(R.drawable.border_cardview_background, Color.parseColor(note.color),false)
            }else{
                linearLayout.setBackgroundResource(R.drawable.border_cardview_background)
            }
        }
    }
}