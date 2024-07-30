package com.example.myapplication.note.formattingBar

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText
import org.xml.sax.XMLReader
import java.util.regex.Pattern


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
        editText.setSelection(end)
    }
    fun applyForegroundColor(editText: EditText, start: Int, end: Int, color : Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    fun applyBackgroundColor(editText: EditText, start: Int, end: Int, color : Int){
        val spannable = editText.text as Spannable
        spannable.setSpan(BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
//    fun applyTextSize(editText: EditText, start: Int, end: Int, size: Float) {
//        val spannable = editText.text as Spannable
//        spannable.setSpan(RelativeSizeSpan(size), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        editText.setSelection(end)
//    }
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
}