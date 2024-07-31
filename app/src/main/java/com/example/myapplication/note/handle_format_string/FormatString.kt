package com.example.myapplication.note.handle_format_string

import android.text.Html
import android.text.Spannable

class FormatString {
    fun spannableToHtml(spannable: Spannable) : String{
        return Html.toHtml(spannable, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    }
    fun htmlToSpannable(html: String): Spannable {
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY) as Spannable
    }
}