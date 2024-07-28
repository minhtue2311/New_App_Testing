package com.example.myapplication.note.options

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.example.myapplication.model.Note
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportNote(private var activity: Activity, private var context : Context, private var onCompleteListener: InterfaceCompleteListener) {
     fun readNoteFromFile(uri: Uri) {
        try {
            activity.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                val fileName = getFileName(uri)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateString = sdf.format(Date())
                val noteModel = Note(fileName, content, dateString, dateString)
                noteModel.label = fileName
                onCompleteListener.onCompleteListener(noteModel)
            } ?: run {
                Toast.makeText(context, "Error opening file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = ""
        activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}