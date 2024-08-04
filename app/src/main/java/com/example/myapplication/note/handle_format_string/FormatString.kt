package com.example.myapplication.note.handle_format_string

import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.core.text.HtmlCompat
import java.util.regex.Pattern

class FormatString {
    fun spannableToHtml(spannable: Spannable): String {
        val sb = StringBuilder()
        var i = 0
        while (i < spannable.length) {
            val next = spannable.nextSpanTransition(i, spannable.length, CharacterStyle::class.java)
            val spans = spannable.getSpans(i, next, CharacterStyle::class.java)

            for (span in spans) {
                when (span) {
                    is AbsoluteSizeSpan -> sb.append("<font size=\"${span.size}\">")
                    is StyleSpan -> {
                        if (span.style and Typeface.BOLD != 0) {
                            sb.append("<b>")
                        }
                        if (span.style and Typeface.ITALIC != 0) {
                            sb.append("<i>")
                        }
                    }
                    is UnderlineSpan -> sb.append("<u>")
                    is StrikethroughSpan -> sb.append("<strike>")
                    is ForegroundColorSpan -> sb.append("<span style=\"color:#${String.format("%06X", 0xFFFFFF and span.foregroundColor)};\">")
                    is BackgroundColorSpan -> sb.append("<span style=\"background-color:#${String.format("%06X", 0xFFFFFF and span.backgroundColor)};\">")
                }
            }

            sb.append(spannable.subSequence(i, next))

            for (span in spans.reversed()) {
                when (span) {
                    is AbsoluteSizeSpan -> sb.append("</font>")
                    is StyleSpan -> {
                        if (span.style and Typeface.BOLD != 0) {
                            sb.append("</b>")
                        }
                        if (span.style and Typeface.ITALIC != 0) {
                            sb.append("</i>")
                        }
                    }
                    is UnderlineSpan -> sb.append("</u>")
                    is StrikethroughSpan -> sb.append("</strike>")
                    is ForegroundColorSpan -> sb.append("</span>")
                    is BackgroundColorSpan -> sb.append("</span>")
                }
            }
            i = next
        }
        return sb.toString()
    }
    fun htmlToSpannable(html: String): Spannable {
        val spannableString = SpannableStringBuilder(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY))

        // Tìm và áp dụng kích thước font từ HTML
        val fontSizePattern = Pattern.compile("<font size=\"(\\d+)\">(.*?)</font>")
        val matcher = fontSizePattern.matcher(html)

        while (matcher.find()) {
            val fontSize = matcher.group(1)?.toInt() ?: 16
            val content = matcher.group(2) ?: ""

            // Tìm vị trí của content trong Spannable
            var startIndex = spannableString.indexOf(content)
            while (startIndex != -1) {
                val endIndex = startIndex + content.length

                // Áp dụng AbsoluteSizeSpan cho đoạn văn bản
                spannableString.setSpan(AbsoluteSizeSpan(fontSize, true), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                // Tìm tiếp các occurrences của content trong Spannable
                startIndex = spannableString.indexOf(content, endIndex)
            }
        }

        return spannableString
    }

    fun compareSpannableDetailed(sp1: Spannable, sp2: Spannable): Boolean {
        if (sp1.length != sp2.length) {
            Log.d("CompareSpannable", "Length mismatch: ${sp1.length} != ${sp2.length}")
            return false
        }

        val spans1 = sp1.getSpans(0, sp1.length, CharacterStyle::class.java)
        val spans2 = sp2.getSpans(0, sp2.length, CharacterStyle::class.java)

        if (spans1.size != spans2.size) {
            Log.d("CompareSpannable", "Span size mismatch: ${spans1.size} != ${spans2.size}")
            return false
        }

        for (i in spans1.indices) {
            if (sp1.getSpanStart(spans1[i]) != sp2.getSpanStart(spans2[i])) {
                Log.d("CompareSpannable", "Span start mismatch for span $i: ${sp1.getSpanStart(spans1[i])} != ${sp2.getSpanStart(spans2[i])}")
                return false
            }
            if (sp1.getSpanEnd(spans1[i]) != sp2.getSpanEnd(spans2[i])) {
                Log.d("CompareSpannable", "Span end mismatch for span $i: ${sp1.getSpanEnd(spans1[i])} != ${sp2.getSpanEnd(spans2[i])}")
                return false
            }
            if (spans1[i] != spans2[i]) {
                Log.d("CompareSpannable", "Span instance mismatch for span $i: ${spans1[i]} != ${spans2[i]}")
                return false
            }
        }

        return sp1.toString() == sp2.toString()
    }

}