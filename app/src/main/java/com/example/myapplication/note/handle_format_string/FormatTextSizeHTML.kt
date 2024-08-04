package com.example.myapplication.note.handle_format_string

import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.style.AbsoluteSizeSpan
import org.xml.sax.XMLReader

class FormatTextSizeHTML : Html.TagHandler {

    override fun handleTag(
        opening: Boolean,
        tag: String?,
        output: Editable?,
        xmlReader: XMLReader?
    ) {
        if (tag.equals("font", ignoreCase = true)) {
            if (opening) {
                val attributes = getAttributes(xmlReader)
                val size = attributes["size"]?.toIntOrNull() ?: 16
                startFont(output, size)
            } else {
                endFont(output)
            }
        }
    }

    private fun startFont(text: Editable?, size: Int) {
        val len = text?.length ?: return
        text.setSpan(AbsoluteSizeSpan(size), len, len, Spannable.SPAN_MARK_MARK)
    }

    private fun endFont(text: Editable?) {
        val len = text?.length ?: return
        val obj = getLast(text, AbsoluteSizeSpan::class.java)
        val where = text.getSpanStart(obj)

        text.removeSpan(obj)

        if (where != len) {
            text.setSpan(obj, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun getAttributes(xmlReader: XMLReader?): Map<String, String> {
        val attributes = mutableMapOf<String, String>()
        try {
            val elementField = xmlReader?.javaClass?.getDeclaredField("theNewElement")
            elementField?.isAccessible = true
            val element = elementField?.get(xmlReader)
            val attsField = element?.javaClass?.getDeclaredField("theAtts")
            attsField?.isAccessible = true
            val atts = attsField?.get(element)
            val dataField = atts?.javaClass?.getDeclaredField("data")
            dataField?.isAccessible = true
            val data = dataField?.get(atts) as? Array<String>
            val lengthField = atts?.javaClass?.getDeclaredField("length")
            lengthField?.isAccessible = true
            val len = lengthField?.get(atts) as? Int ?: 0

            for (i in 0 until len) {
                attributes[data?.get(i * 5 + 1) ?: ""] = data?.get(i * 5 + 4) ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return attributes
    }

    private fun getLast(text: Editable?, kind: Class<*>): Any? {
        val objs = text?.getSpans(0, text.length, kind)
        return if (objs?.isNotEmpty() == true) objs[objs.size - 1] else null
    }
}
