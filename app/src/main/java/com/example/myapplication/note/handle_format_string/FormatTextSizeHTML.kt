package com.example.myapplication.note.handle_format_string

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.AbsoluteSizeSpan
import android.text.style.ReplacementSpan
import org.xml.sax.XMLReader
import java.util.Stack
import java.util.regex.Pattern

class FormatTextSizeHTML : Html.TagHandler {

    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        if (tag.equals("span", ignoreCase = true)) {
            if (opening) {
                val start = output?.length ?: 0
                output?.setSpan(StartTagMarker(), start, start, Spannable.SPAN_MARK_MARK)
            } else {
                val end = output?.length ?: 0
                val spanMarkers = output?.getSpans(0, end, StartTagMarker::class.java)
                if (spanMarkers != null && spanMarkers.isNotEmpty()) {
                    val start = output.getSpanStart(spanMarkers[0])
                    output.removeSpan(spanMarkers[0])

                    // Xác định font-size
                    val spanContent = output.subSequence(start, end).toString()
                    val fontSize = parseFontSize(spanContent)

                    // Áp dụng AbsoluteSizeSpan nếu xác định được font-size
                    if (fontSize != null) {
                        output.setSpan(AbsoluteSizeSpan(fontSize, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
    }

    private fun parseFontSize(text: String): Int? {
        val matcher = Pattern.compile("font-size:\\s*(\\d+)px", Pattern.CASE_INSENSITIVE).matcher(text)
        return if (matcher.find()) matcher.group(1)?.toInt() else null
    }

    private class StartTagMarker
}
