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
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterForPickColor
import com.example.myapplication.adapter.AdapterSelectCategory
import com.example.myapplication.databinding.LayoutColorPickerBinding
import com.example.myapplication.databinding.LayoutCustomSelectCategoryBinding
import com.example.myapplication.databinding.LayoutDetailNoteBinding
import com.example.myapplication.databinding.LayoutShowInfoNoteBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.model.relation.NoteCategoryRef
import com.example.myapplication.note.formattingBar.FormattingBarFunctions
import com.example.myapplication.note.options.ExportNote
import com.example.myapplication.preferences.NoteStatusPreferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack


class DetailNoteFragment : Fragment() {
    private lateinit var viewBinding: LayoutDetailNoteBinding
    private var note: Note? = null
    private var type: String = "Create"
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var preferences : NoteStatusPreferences
    private var colorInstant = "#F2EDC0"
    private var undoStack : Stack<String> = Stack()
    private var rootValue : String = ""
    private var valueBeforeSearch : String = ""
    private var isChangingCharacter = false
    private var isCreatedData = false
    private var listCategories : ArrayList<Categories> = ArrayList()
    private var listCategoriesSelected : ArrayList<Categories> = ArrayList()
    private var categoriesInsertForNote : Categories? = null
    private var readMode = false
    private var clickCount = 0
    private val doubleClickThreshold = 500L // Thời gian ngưỡng cho nhấn đúp (ms)
    private val handler = Handler(Looper.getMainLooper())

    //Variable for state formatting bar
    private var startIndex = 0
    private var isBold = false

    override fun onCreate(savedInstanceState: Bundle?) {
        noteDatabase = NoteDatabase.getInstance(requireContext())
        preferences = NoteStatusPreferences(requireContext())
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutDetailNoteBinding.inflate(inflater, container, false)
        getData()
        getStatusFormattingBar()
        getCategoriesData()
        getCategoriesForThisNote()
        initOriginalValueForEditText()
        handleEditTextContent()
        return viewBinding.root
    }
    override fun onResume() {
        viewBinding.detailFragmentLayout.setOnClickListener {
            //TO DO
        }
        viewBinding.editTextContent.setOnClickListener {
            if(readMode){
                clickCount++
                if (clickCount == 1) {
                    Toast.makeText(requireContext(), "Tap Twice To Edit", Toast.LENGTH_SHORT).show()
                    handler.postDelayed({
                        clickCount = 0
                    }, doubleClickThreshold)
                } else if (clickCount == 2) {
                    readMode = false
                    setReadOnlyMode()
                    clickCount = 0
                    handler.removeCallbacksAndMessages(null)
                }
            }
        }
        viewBinding.backFromEditMode.setOnClickListener {
            backToHomeScreen()
        }
        viewBinding.backFromReadMode.setOnClickListener {
            backToHomeScreen()
        }
        viewBinding.backFromSearch.setOnClickListener {
            viewBinding.editTextContent.setText(valueBeforeSearch)
            viewBinding.layoutTitle.visibility = View.VISIBLE
            viewBinding.layoutSearchToolBar.visibility = View.GONE
        }
        viewBinding.txtSave.setOnClickListener {
            handleSavingData()
        }
        viewBinding.optionsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
        viewBinding.optionsButtonReadMode.setOnClickListener { view ->
            showPopupMenuInReadMode(view)
        }
        viewBinding.btnDownload.setOnClickListener {
            openDocumentTree()
        }
        viewBinding.btnChangeToEditMode.setOnClickListener {
            readMode = false
            setReadOnlyMode()
        }
        viewBinding.btnUndo.setOnClickListener {
            undoFunction(rootValue)
        }
        viewBinding.clearFormattingButton.setOnClickListener{
            viewBinding.linearLayoutFormatingBar.visibility = View.GONE
            preferences.putStatusFormattingBar(false)
        }
        handleOnClickItemFormattingBar()
        super.onResume()
    }

    private fun backToHomeScreen(){
        if(!isCreatedData) {
            handleSavingData()
        }
        requireActivity().supportFragmentManager.popBackStack()
    }
    private fun getData() {
        val bundle = arguments
        if (bundle != null) {
            if(bundle.getParcelable<Note>("Note") != null){
                note = bundle.getParcelable<Note>("Note")
                bindData()
                type = "Update"
            }
            if(bundle["CategoryForNote"] != null){
                categoriesInsertForNote = bundle["CategoryForNote"] as Categories
            }
        }else{
            viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
            viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }
    private fun getCategoriesForThisNote() {
        if(note != null){
            noteDatabase.noteDao().getNoteWithCategories(note!!.idNote!!).observe(viewLifecycleOwner){
                if(it != null){
                    for(category in it.listCategories){
                        listCategoriesSelected.add(category)
                    }
                }
            }
        }
    }

    private fun getCategoriesData() {
       noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner){
            listCategories.addAll(it)
       }
    }
    private fun getStatusFormattingBar(){
        if(preferences.getStatusFormattingBar()){
            viewBinding.linearLayoutFormatingBar.visibility = View.VISIBLE
        }else{
            viewBinding.linearLayoutFormatingBar.visibility = View.INVISIBLE
        }
    }
    private fun handleOnClickItemFormattingBar() {
        viewBinding.boldButton.setOnClickListener {
            if(isBold){
                viewBinding.boldButton.setBackgroundColor(resources.getColor(R.color.colorItem))
                isBold = false
            }else{
                viewBinding.boldButton.setBackgroundColor(resources.getColor(R.color.colorBackgroundButton))
                isBold = true
                startIndex = viewBinding.editTextContent.text.length
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
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
                noteModel.isBold = isBold
                GlobalScope.launch(Dispatchers.IO){
                   val insertNoteId = noteDatabase.noteDao().insertNoteAndGetId(noteModel)
                    categoriesInsertForNote?.let {
                        val crossRef = NoteCategoryRef(insertNoteId.toInt(), it.idCategory!!)
                        noteDatabase.noteDao().insertNoteCategoryCrossRef(crossRef)
                    }
                }

                isCreatedData = true
                Toast.makeText(requireContext(), "Save", Toast.LENGTH_SHORT).show()
            }

            "Update" -> {

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
                    note?.isBold = isBold
                    note?.let { noteDatabase.noteDao().updateNote(it) }
                    Toast.makeText(requireContext(), "Save", Toast.LENGTH_SHORT).show()

            }
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
        if(note?.isBold!!){
            startIndex = viewBinding.editTextContent.text.length
            Log.d("Start Index", startIndex.toString())
            if(note?.color != ""){
                viewBinding.boldButton.setBackgroundColor(makeColorDarker(Color.parseColor(note?.color)))
            }else{
                viewBinding.boldButton.setBackgroundColor(resources.getColor(R.color.colorBackgroundButton))
            }
            isBold = true
        }
    }
    private fun handleColorTitleLayout(linearLayout: LinearLayout){
        if(note?.color != ""){
            linearLayout.background = ColorDrawable(makeColorDarker(Color.parseColor(note?.color)))
        }else{
            linearLayout.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }
    //Make color background more deeper
    private fun makeColorDarker(color : Int) : Int{
        val hsv = FloatArray(3)
        Color.colorToHSV(color,hsv)
        hsv[2] *= 0.4f
        return Color.HSVToColor(hsv)
    }
    private fun initOriginalValueForEditText(){
        rootValue = viewBinding.editTextContent.text.toString()
    }

    private fun handleEditTextContent() {
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
                if(isBold && s != null){
                    val start = startIndex
                    val end = s.length
                    if(start < end){
                        Log.d("Index Start end :", start.toString() + end.toString())
                        removeTextWatcher()
                        FormattingBarFunctions().applyBold(viewBinding.editTextContent, start,end)
                        addTextWatcher()
                        startIndex = end
                    }
                }
            }
            private fun removeTextWatcher() {
                viewBinding.editTextContent.removeTextChangedListener(this)
            }

            private fun addTextWatcher() {
                viewBinding.editTextContent.addTextChangedListener(this)
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

            if (Html.toHtml(SpannableStringBuilder(viewBinding.editTextContent.text))== initString) {
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
        //Handle undo menu item
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
        //Handle formatting bar item
        val formattingBarMenuItem = popupMenu.menu.findItem(R.id.open_formatting_bar)
        if(preferences.getStatusFormattingBar()){
            formattingBarMenuItem.isEnabled = false
            formattingBarMenuItem.title = formattingBarMenuItem.title.toString().apply {
                formattingBarMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.BLACK), 0, length, 0)
                }
            }
        }else {
            formattingBarMenuItem.isEnabled = true
            formattingBarMenuItem.title = formattingBarMenuItem.title.toString().apply {
                formattingBarMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.GRAY), 0, length, 0)
                }
            }
        }
        popupMenu.show()
    }
    private fun showPopupMenuInReadMode(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_options_item_in_read_mode, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
            true
        }
        popupMenu.show()
    }
    private fun setReadOnlyMode() {
        viewBinding.editTextTitle.isFocusable = !readMode
        viewBinding.editTextTitle.isFocusableInTouchMode = !readMode
        viewBinding.editTextTitle.isCursorVisible = !readMode

        viewBinding.editTextContent.isFocusable = !readMode
        viewBinding.editTextContent.isFocusableInTouchMode = !readMode
        viewBinding.editTextContent.isCursorVisible = !readMode

        if (readMode) {
            viewBinding.editTextTitle.clearFocus()
            viewBinding.editTextContent.clearFocus()
            viewBinding.layoutTitleReadMode.visibility = View.VISIBLE
            viewBinding.layoutTitle.visibility = View.GONE
            handleColorTitleLayout(viewBinding.layoutTitleReadMode)
        }
        else{
            viewBinding.layoutTitleReadMode.visibility = View.GONE
            viewBinding.layoutTitle.visibility = View.VISIBLE
        }
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_delete , R.id.action_delete_read_mode -> {
                showDialogConfirm()
            }

            R.id.action_Categories , R.id.action_Categories_read_mode -> {
                showDialogCategory()
            }

            R.id.action_colorize , R.id.action_colorize_read_mode-> {
                showDialogPickColor()
            }
            R.id.search_menu -> {
                viewBinding.layoutSearchToolBar.visibility = View.VISIBLE
                viewBinding.layoutTitle.visibility = View.GONE
                viewBinding.searchBar.isFocusable = true
                valueBeforeSearch = viewBinding.editTextContent.text.toString()
                handleColorTitleLayout(viewBinding.layoutSearchToolBar)
                handleSearchInNote()
            }

            R.id.show_info , R.id.show_info_read_mode -> {
                showDialogInfo()
            }
            R.id.undoAll -> {
                showUndoAllDialog()
            }
            R.id.exportTxtFiles ->{
                openDocumentTree()
            }
            R.id.switchToReadMode ->{
                readMode = true
                setReadOnlyMode()
            }
            R.id.share_menu -> {
                openIntentForShareOutSide()
            }
            R.id.open_formatting_bar ->{
                viewBinding.linearLayoutFormatingBar.visibility = View.VISIBLE
                preferences.putStatusFormattingBar(true)
            }
        }
    }

    private fun openIntentForShareOutSide() {
        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, note?.content)
        sendIntent.setType("text/plain")
        val shareIntent = Intent.createChooser(sendIntent, "Share with")
        startActivity(shareIntent)
    }

    private fun handleSearchInNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                highlightMatches(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }
    private fun highlightMatches(searchText: String) {
        val content: String = viewBinding.editTextContent.text.toString()
        val spannableString = SpannableString(content)

        if (searchText.isNotEmpty()) {
            var index = content.indexOf(searchText)
            while (index >= 0) {
                spannableString.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    index,
                    index + searchText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = content.indexOf(searchText, index + searchText.length) // search for next character.
            }
        }

        viewBinding.editTextContent.setText(spannableString)
    }

    private fun showDialogCategory() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutCustomSelectCategoryBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val listCategoriesSelectedBackup = ArrayList<Categories>()
        listCategoriesSelectedBackup.addAll(listCategoriesSelected)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val adapter = AdapterSelectCategory(listCategories, listCategoriesSelected)
        binding.recyclerViewCheckNote.adapter = adapter
        binding.recyclerViewCheckNote.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewCheckNote.setHasFixedSize(false)
        binding.btnOk.setOnClickListener {
            createNoteCategoryLinks(listCategoriesSelectedBackup)
            dialog.cancel()
        }
        binding.btnCancel.setOnClickListener {
            listCategoriesSelected.clear()
            listCategoriesSelected.addAll(listCategoriesSelectedBackup)
            dialog.cancel()
        }
        dialog.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createNoteCategoryLinks(listCategoriesInitial : ArrayList<Categories>) {
        val noteDao = noteDatabase.noteDao()
        if(listCategoriesSelected.size != 0){
            GlobalScope.launch(Dispatchers.IO) {
                for(categoryInitial in listCategoriesInitial){
                    if(!listCategoriesSelected.contains(categoryInitial)){
                        val crossRef = NoteCategoryRef(note!!.idNote!!, categoryInitial.idCategory!!)
                        noteDao.deleteNoteCategoryCrossRef(crossRef)
                    }
                }
                for (category in listCategoriesSelected) {
                    if (note != null) {
                        val linksExist = noteDatabase.noteDao().checkNoteCategoryRefExists(note!!.idNote!!, category.idCategory!!)
                        if(linksExist == 0){
                            val crossRef = NoteCategoryRef(note!!.idNote!!, category.idCategory!!)
                            noteDao.insertNoteCategoryCrossRef(crossRef)
                        }
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Updated Categorize", Toast.LENGTH_SHORT).show()
            }
        }else{
            Log.d("Error : ", "Error when updated categorize")
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
                note?.let {
                    noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(note?.idNote!!)
                    noteDatabase.noteDao().delete(it)
                }
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
        val colors = listOf("#ffadaf","#ffd7a6","#fdffb6","#cbffc1","#9cf6ff","#a1c3ff",
            "#bdb2ff","#ffc7ff",
            "#fffffb","#d6e6ff","#d7f9f8","#ffffea","#fff0d3","#fcdfe1",
            "#e4d4ef","#809cce","#94b8d0","#b8e0d5","#d6eadf","#eac3d5","#8c5193","#be4f91",
            "#fe6361","#ff8530","#fea601","#ffd380")
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

            override fun onClickCategoriesItem(categories: Categories) {

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