package com.example.myapplication.note.options
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import com.example.myapplication.model.Note
import java.io.OutputStream

class ExportNote(private var context : Context, private var activity: Activity) {
    fun openDocumentTree(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        activity.startActivityForResult(intent, 1)
    }
    fun saveNoteToDocument(uri: Uri, note : Note?) {
        val contentResolver = activity.contentResolver // Su dung content resolver de truy cap tai nguyen ben ngoai ung dung
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)    //Tao ra docUri tu uri cua thu muc da chon.
        )
        if (note != null) {
            try {
                val fileName = "${note.label}.txt"
                val newUri = DocumentsContract.createDocument(  //Tao file moi tren docUri do, su dung contentResolver, voi dinh dang file la text/plain, va fileName da de ra.
                    contentResolver,
                    docUri,
                    "text/plain",
                    fileName
                )
                if (newUri != null) {
                    contentResolver.openOutputStream(newUri).use { outputStream ->   // Goi den openOutputStream de cho phep viet tren uri do, dong thoi goi toi ham writeNoteToStream de tien hanh ghi de len tep
                        if (outputStream != null) {
                            writeNoteToStream(note!!, outputStream)
                            Toast.makeText(context, "File saved to $fileName", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error opening output stream", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error creating document", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }else{
            try {
                val fileName = "Untitled.txt"
                val newUri = DocumentsContract.createDocument(  //Tao file moi tren docUri do, su dung contentResolver, voi dinh dang file la text/plain, va fileName da de ra.
                    contentResolver,
                    docUri,
                    "text/plain",
                    fileName
                )
                if (newUri != null) {
                    contentResolver.openOutputStream(newUri).use { outputStream ->   // Goi den openOutputStream de cho phep viet tren uri do, dong thoi goi toi ham writeNoteToStream de tien hanh ghi de len tep
                        if (outputStream != null) {
                            outputStream.write("".toByteArray())
                            Toast.makeText(context, "File saved to $fileName", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Error opening output stream", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error creating document", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun writeNoteToStream(note: Note, outputStream: OutputStream) {
        outputStream.write(note.content.toByteArray())
        outputStream.flush()
        outputStream.close()
    }
    fun saveListNoteToDocument(uri: Uri, listNote : ArrayList<Note>){
        val contentResolver = activity.contentResolver
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )

        try {
            for (note in listNote) {
                val fileName = "${note.label}.txt"
                val newUri = DocumentsContract.createDocument(
                    contentResolver,
                    docUri,
                    "text/plain",
                    fileName
                )

                if (newUri != null) {
                    contentResolver.openOutputStream(newUri).use { outputStream ->
                        if (outputStream != null) {
                            writeNoteToStream(note, outputStream)
                        } else {
                            Toast.makeText(context, "Error opening output stream", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Error creating document", Toast.LENGTH_SHORT).show()
                }
            }
            Toast.makeText(context, "Export ${listNote.size} txt File", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}