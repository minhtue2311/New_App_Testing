package com.example.myapplication.note.formattingBar

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText


class FormattingBarFunctions {
     fun applyBold(editText : EditText, start : Int, end : Int) {
         val spannable = editText.text as Spannable
         spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
         editText.setSelection(end)
    }
     fun applyItalic(editText: EditText){
        val start: Int = editText.selectionStart
        val end: Int = editText.selectionEnd
        val ssb = SpannableStringBuilder(editText.text)
        ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editText.text = ssb
    }
     fun applyUnderline(editText: EditText){
        val start: Int = editText.selectionStart
        val end: Int = editText.selectionEnd
        val ssb = SpannableStringBuilder(editText.text)
        ssb.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editText.text = ssb
    }
     fun applyStrikethrough(editText: EditText){
        val start: Int = editText.selectionStart
        val end: Int = editText.selectionEnd
        val ssb = SpannableStringBuilder(editText.text)
        ssb.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editText.text = ssb
    }
}