package com.example.myapplication.note.formatting_bar

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText


class FormattingBarFunctions {
     fun applyBold(editText : EditText, start : Int, end : Int) {
         val spannable = editText.text as Spannable
         spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
     fun applyItalic(editText: EditText, start: Int, end: Int){
         val spannable = editText.text as Spannable
        spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
     fun applyUnderline(editText: EditText, start: Int, end: Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
     fun applyStrikethrough(editText: EditText, start: Int, end: Int){
        val ssb = editText.text as Spannable
        ssb.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    fun applyForegroundColor(editText: EditText, start: Int, end: Int, color : Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    fun applyBackgroundColor(editText: EditText, start: Int, end: Int, color : Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    fun applyTextSize(editText: EditText, start: Int, end: Int, textSize : Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(AbsoluteSizeSpan(textSize, true), start,end ,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    fun removeSpan(editable: Editable, spanClass: Class<out CharacterStyle>, style: Int, start : Int, end : Int) {
        val spans = editable.getSpans(start, end, spanClass)
        for (span in spans) {
            if ((span as StyleSpan).style == style) {
                editable.removeSpan(span)
            }
        }
    }
     fun highlightMatches(searchText: String, editText: EditText) {
         val content: String = editText.text.toString()
         val spannableString = SpannableString(content)

         if (searchText.isNotEmpty()) {
             var index = content.indexOf(searchText)
             while (index >= 0) {
                 spannableString.setSpan(
                     BackgroundColorSpan(Color.YELLOW),
                     index,
                     index + searchText.length,
                     Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                 )
                 index = content.indexOf(
                     searchText,
                     index + searchText.length
                 ) // search for next character.
             }
         }
         editText.setText(spannableString)
     }
     fun checkBoldSpans(editable : Editable, start : Int, end : Int) : Boolean{
         val boldSpans = editable.getSpans(start, end, StyleSpan::class.java)
         val isBold = boldSpans.any { it.style == Typeface.BOLD }
         return isBold
     }
    fun checkItalicSpans(editable : Editable, start : Int, end : Int) : Boolean{
        val italicSpans = editable.getSpans(start, end, StyleSpan::class.java)
        val isItalic = italicSpans.any { it.style == Typeface.ITALIC }
        return isItalic
    }
    fun checkUnderlineSpans(editable : Editable, start : Int, end : Int) : Boolean{
        val underlineSpans = editable.getSpans(start, end, UnderlineSpan::class.java)
        val isUnderline = underlineSpans.isNotEmpty()
        return isUnderline
    }
    fun checkStrikeThroughSpans(editable : Editable, start : Int, end : Int) : Boolean{
        val strikethroughSpans = editable.getSpans(start, end, StrikethroughSpan::class.java)
        val isStrikethrough = strikethroughSpans.isNotEmpty()
        return isStrikethrough
    }
    fun checkTextSizeSpans(editable: Editable, start: Int, end: Int): Boolean {
        val sizeSpans = editable.getSpans(start, end, AbsoluteSizeSpan::class.java)
        val isSize18f = sizeSpans.any { it.size != 18 }
        return isSize18f
    }

    fun getForegroundColorText(editable : Editable, start : Int, end : Int) : Int{
        val colorSpans = editable.getSpans(start, end, ForegroundColorSpan::class.java)
        val foregroundColors = colorSpans.map {
            it.foregroundColor
        }
        val colorForegroundToSet = foregroundColors.lastOrNull() ?: Color.TRANSPARENT
        return colorForegroundToSet
    }
    fun getBackgroundColorText(editable : Editable, start : Int, end : Int) : Int{
        val backgroundSpans = editable.getSpans(start, end, BackgroundColorSpan::class.java)
        val backgroundColors = backgroundSpans.map { it.backgroundColor }
        val colorBackgroundToSet = backgroundColors.lastOrNull() ?: Color.TRANSPARENT
        return colorBackgroundToSet
    }
}