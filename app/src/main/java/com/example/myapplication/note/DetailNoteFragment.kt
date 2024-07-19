package com.example.myapplication.note

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterForPickColor
import com.example.myapplication.databinding.LayoutColorPickerBinding
import com.example.myapplication.databinding.LayoutDetailNoteBinding
import com.example.myapplication.databinding.LayoutShowInfoNoteBinding
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.option.ExportNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack

class DetailNoteFragment : Fragment() {
    private lateinit var viewBinding: LayoutDetailNoteBinding
    private var note: Note? = null
    private var type: String = "Create"
    private lateinit var noteDatabase: NoteDatabase
    private var colorInstant = "#F2EDC0"
    private var undoStack : Stack<String> = Stack()
    private var rootValue : String = ""
    private var isChangingCharacter = false
    private var isCreatedData = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutDetailNoteBinding.inflate(inflater, container, false)
        getData()
        noteDatabase = NoteDatabase.getInstance(requireContext())
        viewBinding.detailFragmentLayout.setOnClickListener {
            //TO DO
        }
        viewBinding.back.setOnClickListener {
            if(!isCreatedData) {
                handleSavingData()
            }
            requireActivity().supportFragmentManager.popBackStack()
        }
        viewBinding.txtSave.setOnClickListener {
            handleSavingData()
        }
        viewBinding.optionsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
        initOriginalValueForEditText()
        handleUndoEditText()
        viewBinding.btnUndo.setOnClickListener {
            undoFunction(rootValue)
        }

        return viewBinding.root
    }
    private fun handleSavingData(){
        when (type) {
            "Create" -> {
                var title = ""
                var content = ""
                if (viewBinding.editTextTitle.text.isNotEmpty()) {
                    title = viewBinding.editTextTitle.text.toString()
                }
                if (viewBinding.editTextContent.text.isNotEmpty()) {
                    content = viewBinding.editTextContent.text.toString()
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateString = sdf.format(Date())
                val noteModel = Note(title, content, dateString, dateString)
                if(colorInstant != "#F2EDC0"){
                    noteModel.color = colorInstant
                }
                if(title == "" && content == ""){
                    noteModel.label = "Untitled"
                }
                else if(title == ""){
                    noteModel.label = content
                }
                else {
                    noteModel.label = title
                }
                noteDatabase.noteDao().insertNote(noteModel)
                isCreatedData = true
                Toast.makeText(requireContext(), "Save", Toast.LENGTH_SHORT).show()
            }

            "Update" -> {
                if(note?.title != viewBinding.editTextTitle.text.toString() || note?.content != viewBinding.editTextContent.text.toString()){
                    val titleValue = viewBinding.editTextTitle.text.toString()
                    val contentValue = viewBinding.editTextContent.text.toString()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateString = sdf.format(Date())
                    note?.apply {
                        title = titleValue
                        content = contentValue
                        editedDate = dateString
                    }
                    if(titleValue == "" && contentValue == ""){
                        note?.label = "Untitled"
                    }
                    else if(titleValue == ""){
                        note?.label = contentValue
                    }
                    else {
                        note?.label = titleValue
                    }
                    note?.let { noteDatabase.noteDao().updateNote(it) }
                    Toast.makeText(requireContext(), "Save", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getData() {
        val bundle = arguments
        if (bundle != null) {
            note = bundle.getParcelable<Note>("Note")
            bindData()
            type = "Update"
        }else{
            viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
            viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }

    private fun bindData() {
        viewBinding.editTextTitle.setText(note?.title.toString())
        viewBinding.editTextContent.setText(note?.content.toString())
        if(note?.color != ""){
            viewBinding.linearLayoutDetailNote.setBackgroundColor(Color.parseColor(note?.color))
            viewBinding.layoutTitle.background = ColorDrawable(makeColorDarker(Color.parseColor(note?.color)))
        }else{
            viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
            viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }
    //Make color background more deeper
    private fun makeColorDarker(color : Int) : Int{
        val hsv = FloatArray(3)
        Color.colorToHSV(color,hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
    }
    private fun initOriginalValueForEditText(){
        rootValue = viewBinding.editTextContent.text.toString()
    }

    private fun handleUndoEditText() {
        val textWatcher = object : TextWatcher {
            private var previousText: String = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Save the current text before it changes
                previousText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the undo button color if there is a change
                if (s.toString() != rootValue) {
                    viewBinding.btnUndo.setTextColor(resources.getColor(R.color.color1))
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Add the previous text to the stack only if the current text is different than the older
                if (s.toString() != previousText && !isChangingCharacter) {
                    undoStack.push(previousText)
                }
            }
        }

        viewBinding.editTextContent.addTextChangedListener(textWatcher)
    }
    private fun undoFunction(initString: String) {
        if (undoStack.isNotEmpty()) {
            val previousText = undoStack.pop()
            isChangingCharacter = true // Set the flag to indicate undo is in progress
            viewBinding.editTextContent.setText(previousText)
            viewBinding.editTextContent.setSelection(previousText.length)
            isChangingCharacter = false // Reset the flag after undo is done

            if (viewBinding.editTextContent.text.toString() == initString) {
                viewBinding.btnUndo.setTextColor(Color.parseColor("#A8A5A5"))
            }
        }
    }
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_options_item_note, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
            true
        }
        val undoAllMenuItem = popupMenu.menu.findItem(R.id.undoAll)
        if(rootValue == "" || rootValue == viewBinding.editTextContent.text.toString()){
            undoAllMenuItem.isEnabled = false
            undoAllMenuItem.title = undoAllMenuItem.title.toString().apply {
                undoAllMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.GRAY),0,length,0)
                }
            }
        }else{
            undoAllMenuItem.isEnabled = true
            undoAllMenuItem.title = undoAllMenuItem.title.toString().apply {
                undoAllMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.BLACK), 0, length, 0)
                }
            }
        }
        popupMenu.show()
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_delete -> {
                showDialogConfirm()
            }

            R.id.action_Categories -> {

            }

            R.id.action_colorize -> {
                showDialogPickColor()
            }

            R.id.show_info -> {
                showDialogInfo()
            }
            R.id.undoAll -> {
                showUndoAllDialog()
            }
            R.id.exportTxtFiles ->{
                openDocumentTree()
            }
        }
    }

    private fun showUndoAllDialog() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Confirm")
            .setMessage("Remove all the note changes made since the last opening of the note ?")
            .setPositiveButton("UNDO ALL") { dialogInterface, _ ->
                viewBinding.editTextContent.setText(note?.content)
                viewBinding.editTextContent.setSelection(note?.content.toString().length)
                dialogInterface.cancel()
            }
            .setNegativeButton("CANCEL") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    private fun showDialogConfirm() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Confirm")
            .setMessage("Do you want to remove ${note?.label}?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                note?.let { noteDatabase.noteDao().delete(it) }
                dialogInterface.cancel()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showDialogInfo() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutShowInfoNoteBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        binding.words.text = requireContext().getString(R.string.words) + getNumberOfWords(viewBinding.editTextContent.text.toString()).toString()
        binding.lineCount.text = requireContext().getString(R.string.line_count) + viewBinding.editTextContent.lineCount.toString()
        binding.characters.text = requireContext().getString(R.string.characters) + viewBinding.editTextContent.text.toString().length
        binding.charactersWithoutWhiteSpaces.text = requireContext().getString(R.string.characters_without_whitespaces) + getNumberOfCharactersWithoutSpace(viewBinding.editTextContent.text.toString()).toString()
        binding.createdAt.text = requireContext().getString(R.string.created_at) + note?.createDate
        binding.LastSaved.text = requireContext().getString(R.string.last_saved_at) + note?.editedDate
        binding.btnOk.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }
    private fun getNumberOfWords(input : String) : Int{
        val wordCount = input.split("\\s+".toRegex()).size
        return wordCount
    }
    private fun getNumberOfCharactersWithoutSpace(input : String) : Int{
        var result = 0
        val splitResult = input.split(" ")
        for(words in splitResult){
            result += words.length
        }
        return result
    }
    private fun openDocumentTree() {   //Cho phep chon thu muc muon luu tai lieu
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 1 )
    }
    @Deprecated("Deprecated in Java")    //Tra ve ket qua la uri cua thu muc duoc chon
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                ExportNote(requireContext(), requireActivity()).saveNoteToDocument(uri,note)
            }
        }
    }
    private fun showDialogPickColor(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutColorPickerBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        if(note != null){
            if(note!!.color != ""){
                colorInstant = note!!.color
            }
        }
        if(colorInstant != "#F2EDC0"){
            binding.txtSelectColor.setBackgroundColor(Color.parseColor(colorInstant))
        }else {
            binding.txtSelectColor.setBackgroundColor(Color.WHITE)
        }
        //Init List Color
        val colors = listOf("#FB0202","#3F51B6","#8CAF60","#FF9788","#FD5722","#FFC207",
            "#876933","#65C3D1",
            "#2196F3","#3F51B5","#4CAF50","#009688","#FF5722","#FFC107",
            "#873933","#55C3D1")
        binding.recyclerViewPickColor.setHasFixedSize(false)
        val layoutManager = GridLayoutManager(requireContext(), 6)
        binding.recyclerViewPickColor.layoutManager = layoutManager
        val adapter = AdapterForPickColor(colors, object : InterfaceOnClickListener{
            override fun onClickItemNoteListener(note: Note) {

            }

            override fun onClickColorItem(color: String) {
                binding.txtSelectColor.setBackgroundColor(Color.parseColor(color))
                colorInstant = color
            }

            override fun onSelectedNote(listNoteSelectedResult: ArrayList<Note>) {

            }

        })
        binding.recyclerViewPickColor.adapter = adapter

        //Remove color
        binding.btnRemoveColor.setOnClickListener {
            colorInstant = "#F2EDC0"
            binding.txtSelectColor.setBackgroundColor(Color.WHITE)
        }
        binding.btnOk.setOnClickListener {
                if(colorInstant != "#F2EDC0"){
                    if(note != null){
                        note?.color = colorInstant
                        note?.let { noteDatabase.noteDao().updateNote(it) }
                        viewBinding.linearLayoutDetailNote.setBackgroundColor(Color.parseColor(colorInstant))
                        viewBinding.layoutTitle.background = ColorDrawable(makeColorDarker(Color.parseColor(colorInstant)))
                    }else {
                        viewBinding.linearLayoutDetailNote.setBackgroundColor(Color.parseColor(colorInstant))
                        viewBinding.layoutTitle.background = ColorDrawable(makeColorDarker(Color.parseColor(colorInstant)))
                    }
                }
                else{
                   if(note != null){
                       note?.color = ""
                       note?.let { noteDatabase.noteDao().updateNote(it) }
                       viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
                       viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
                   }else{
                       viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
                       viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
                   }
                }
            dialog.cancel()
        }
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }
}