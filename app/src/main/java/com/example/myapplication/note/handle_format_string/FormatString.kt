package com.example.myapplication.note.handle_format_string


import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.HtmlCompat

class FormatString {
    fun spannableToHtml(spannable: Spannable): String {
        return Html.toHtml(spannable, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
    fun htmlToSpannable(html: String): Spannable {
        val spannableString = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT))
        return spannableString
    }
}