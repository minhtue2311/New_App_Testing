package com.example.myapplication.note.options

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.R
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportExportManager(
    private val activity: FragmentActivity,
    private val context: Context,
) {
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>

    fun setImportLauncher(importLauncher : ActivityResultLauncher<Intent>){
        this.importLauncher = importLauncher
    }
    fun setExportLauncher(exportLauncher : ActivityResultLauncher<Intent>){
        this.exportLauncher = exportLauncher
    }

    fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        importLauncher.launch(intent)
    }

    fun openDocumentTreeForListNote(listNote: ArrayList<Note>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        exportLauncher.launch(intent)
    }

    fun openDocumentTreeForNote(note: Note?) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        exportLauncher.launch(intent)
    }

    fun readNoteFromFile(uri: Uri, onCompleteListener: InterfaceCompleteListener) {
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

    fun saveListNoteToDocument(uri: Uri, listNote: ArrayList<Note>) {
        try {
            for (note in listNote) {
                saveNoteToDocument(uri, note)
            }
            Toast.makeText(context, "Export ${listNote.size} txt File", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeNoteToStream(note: Note, outputStream: OutputStream) {
        outputStream.write(note.content.toByteArray())
        outputStream.flush()
        outputStream.close()
    }

    fun saveNoteToDocument(uri: Uri, note: Note?) {
        val contentResolver =
            activity.contentResolver // Su dung content resolver de truy cap tai nguyen ben ngoai ung dung
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)    //Tao ra docUri tu uri cua thu muc da chon.
        )
        if (note != null) {
            try {
                val fileName = "${note.label}.txt"
                val newUri =
                    DocumentsContract.createDocument(  //Tao file moi tren docUri do, su dung contentResolver, voi dinh dang file la text/plain, va fileName da de ra.
                        contentResolver,
                        docUri,
                        "text/plain",
                        fileName
                    )
                if (newUri != null) {
                    contentResolver.openOutputStream(newUri)
                        .use { outputStream ->   // Goi den openOutputStream de cho phep viet tren uri do, dong thoi goi toi ham writeNoteToStream de tien hanh ghi de len tep
                            if (outputStream != null) {
                                writeNoteToStream(note!!, outputStream)
                                Toast.makeText(
                                    context,
                                    "File saved to $fileName",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.errorOpeningStream),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.errorCreatingDocument),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            try {
                val fileName = context.getString(R.string.Untitled)
                val newUri =
                    DocumentsContract.createDocument(  //Tao file moi tren docUri do, su dung contentResolver, voi dinh dang file la text/plain, va fileName da de ra.
                        contentResolver,
                        docUri,
                        "text/plain",
                        fileName
                    )
                if (newUri != null) {
                    contentResolver.openOutputStream(newUri)
                        .use { outputStream ->   // Goi den openOutputStream de cho phep viet tren uri do, dong thoi goi toi ham writeNoteToStream de tien hanh ghi de len tep
                            if (outputStream != null) {
                                outputStream.write("".toByteArray())
                                Toast.makeText(
                                    context,
                                    "File saved to $fileName",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.errorOpeningStream),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.errorCreatingDocument),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}