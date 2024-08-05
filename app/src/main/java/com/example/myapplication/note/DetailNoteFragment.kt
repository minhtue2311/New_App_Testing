package com.example.myapplication.note

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.alpha
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterForPickColor
import com.example.myapplication.adapter.AdapterSelectCategory
import com.example.myapplication.databinding.LayoutColorPickerBinding
import com.example.myapplication.databinding.LayoutCustomSelectCategoryBinding
import com.example.myapplication.databinding.LayoutDetailNoteBinding
import com.example.myapplication.databinding.LayoutDialogChangeTextSizeBinding
import com.example.myapplication.databinding.LayoutPickColorFormattingBarBinding
import com.example.myapplication.databinding.LayoutShowInfoNoteBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.model.relation.NoteCategoryRef
import com.example.myapplication.note.formatting_bar.FormattingBarFunctions
import com.example.myapplication.note.handle_format_string.FormatString
import com.example.myapplication.note.options.ImportExportManager
import com.example.myapplication.preferences.NoteStatusPreferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Stack
import kotlin.reflect.KMutableProperty0


class DetailNoteFragment : Fragment() {
    private lateinit var viewBinding: LayoutDetailNoteBinding
    private var note: Note? = null
    private var type: String = "Create"
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var preferences: NoteStatusPreferences
    private var colorInstant = "#ffffdd"
    private var undoStack: Stack<Spannable> = Stack()
    private var rootValue: String = ""
    private var valueBeforeSearch: String = ""
    private var isChangingCharacter = false
    private var isCreatedData = false
    private var listCategories: ArrayList<Categories> = ArrayList()
    private var listCategoriesSelected: ArrayList<Categories> = ArrayList()
    private var categoriesInsertForNote: Categories? = null
    private var readMode = false
    private var clickCount = 0
    private val doubleClickThreshold = 500L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importExportManager: ImportExportManager

    //Variable for state formatting bar
    private var startIndex = 0
    private var isBoldState = false
    private var isItalicState = false
    private var isUnderlineState = false
    private var isStrikethroughState = false
    private var isChangedTextSize = false
    private var textSizeValue = 18
    private var isChangedColorText = false
    private var isChangedBackgroundText = false
    private var colorText = ""
    private var backgroundColor = ""
    var lastStartSelection: Int = -1
    var lastEndSelection: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        noteDatabase = NoteDatabase.getInstance(requireContext())
        preferences = NoteStatusPreferences(requireContext())
        exportLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        importExportManager.saveNoteToDocument(uri, note)
                    }
                }
            }
        importExportManager = ImportExportManager(requireActivity(), requireContext())
        importExportManager.setExportLauncher(exportLauncher)
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
        viewBinding.detailFragmentLayout.setOnClickListener {
            //TO DO
        }
        viewBinding.editTextContent.setOnClickListener {
            if (readMode) {
                clickCount++
                if (clickCount == 1) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.tap_twice_to_edit),
                        Toast.LENGTH_SHORT
                    ).show()
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
        viewBinding.detailFragmentLayout.setOnClickListener {
            //TO DO
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
            importExportManager.openDocumentTreeForNote(note)
        }
        viewBinding.btnChangeToEditMode.setOnClickListener {
            readMode = false
            setReadOnlyMode()
        }
        viewBinding.btnUndo.setOnClickListener {
            undoFunction(rootValue)
        }
        viewBinding.clearFormattingButton.setOnClickListener {
            viewBinding.linearLayoutFormatingBar.visibility = View.GONE
            preferences.putStatusFormattingBar(false)
        }
        viewBinding.linearLayoutDetailNote.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            viewBinding.linearLayoutDetailNote.getWindowVisibleDisplayFrame(r)
            val screenHeight = viewBinding.linearLayoutDetailNote.rootView.height
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.1) {
                viewBinding.editTextContent.setPadding(0, 0, 0, 1000)
            } else {
                viewBinding.editTextContent.setPadding(0, 0, 0, 1600)
            }
        }
        val checkSelectionRunnable: Runnable = object : Runnable {
            override fun run() {
                val startSelection: Int = viewBinding.editTextContent.selectionStart
                val endSelection: Int = viewBinding.editTextContent.selectionEnd

                if (startSelection != lastStartSelection || endSelection != lastEndSelection) {
                    if (startSelection != endSelection) {
                        updateFormattingButtonsState(startSelection, endSelection)
                    } else {
                        setUpBackgroundForItemInFormattingBar(viewBinding.boldButton, isBoldState)
                        setUpBackgroundForItemInFormattingBar(
                            viewBinding.italicButton,
                            isItalicState
                        )
                        setUpBackgroundForItemInFormattingBar(
                            viewBinding.underlineButton,
                            isUnderlineState
                        )
                        setUpBackgroundForItemInFormattingBar(
                            viewBinding.strikethroughButton,
                            isStrikethroughState
                        )
                        setUpBackgroundForItemInFormattingBar(viewBinding.fontSizeButton, isChangedTextSize)
                        if (colorText != "") {
                            viewBinding.colorTextButton.setBackgroundColor(
                                Color.parseColor(
                                    colorText
                                )
                            )
                        } else {
                            viewBinding.colorTextButton.setBackgroundColor(
                                Color.parseColor(
                                    colorInstant
                                )
                            )
                        }
                        if (backgroundColor != "") {
                            viewBinding.colorBackgroundButton.setBackgroundColor(
                                Color.parseColor(
                                    backgroundColor
                                )
                            )
                        } else {
                            viewBinding.colorBackgroundButton.setBackgroundColor(
                                Color.parseColor(
                                    colorInstant
                                )
                            )
                        }
                    }
                    lastStartSelection = startSelection
                    lastEndSelection = endSelection
                }
                handler.postDelayed(this, 50)
            }
        }
        viewBinding.editTextContent.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                handler.post(checkSelectionRunnable);
            } else {
                handler.removeCallbacks(checkSelectionRunnable)
            }
        }
        handleEditTextContent()
        handleOnClickItemFormattingBar()
        return viewBinding.root
    }

    private fun updateFormattingButtonsState(start: Int, end: Int) {
        val editable = viewBinding.editTextContent.text

        // Check if the selected text has bold
        val isBold = FormattingBarFunctions().checkBoldSpans(editable, start, end)
        setUpBackgroundForItemInFormattingBar(viewBinding.boldButton, isBold)

        // Check if the selected text has italic
        val isItalic = FormattingBarFunctions().checkItalicSpans(editable, start, end)
        setUpBackgroundForItemInFormattingBar(viewBinding.italicButton, isItalic)

        // Check if the selected text has underline
        val isUnderline = FormattingBarFunctions().checkUnderlineSpans(editable, start, end)
        setUpBackgroundForItemInFormattingBar(viewBinding.underlineButton, isUnderline)

        // Check if the selected text has strikethrough
        val isStrikethrough = FormattingBarFunctions().checkStrikeThroughSpans(editable, start, end)
        setUpBackgroundForItemInFormattingBar(viewBinding.strikethroughButton, isStrikethrough)

        //Check if the selected text has TextSize
        val isChangeTextSize = FormattingBarFunctions().checkTextSizeSpans(editable, start, end)
        setUpBackgroundForItemInFormattingBar(viewBinding.fontSizeButton, isChangeTextSize)

        // Check for color spans if needed
        val colorForegroundToSet =
            FormattingBarFunctions().getForegroundColorText(editable, start, end)
        viewBinding.colorTextButton.setBackgroundColor(colorForegroundToSet)

        // Check for background color spans if needed
        val colorBackgroundToSet =
            FormattingBarFunctions().getBackgroundColorText(editable, start, end)
        viewBinding.colorBackgroundButton.setBackgroundColor(colorBackgroundToSet)
    }


    private fun backToHomeScreen() {
        if (!isCreatedData) {
            handleSavingData()
        }
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun getData() {
        val bundle = arguments
        if (bundle != null) {
            if (bundle.getParcelable<Note>("Note") != null) {
                note = bundle.getParcelable<Note>("Note")
                bindData()
                type = "Update"
            }
            if (bundle["CategoryForNote"] != null) {
                categoriesInsertForNote = bundle["CategoryForNote"] as Categories
            }
        } else {
            viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
            viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }

    private fun getCategoriesForThisNote() {
        if (note != null) {
            noteDatabase.noteDao().getNoteWithCategories(note!!.idNote!!)
                .observe(viewLifecycleOwner) {
                    if (it != null) {
                        for (category in it.listCategories) {
                            listCategoriesSelected.add(category)
                        }
                    }
                }
        }
    }

    private fun getCategoriesData() {
        noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner) {
            listCategories.addAll(it)
        }
    }

    private fun getStatusFormattingBar() {
        if (preferences.getStatusFormattingBar()) {
            viewBinding.linearLayoutFormatingBar.visibility = View.VISIBLE
        } else {
            viewBinding.linearLayoutFormatingBar.visibility = View.INVISIBLE
        }
    }

    private fun handleOnClickItemFormattingBar() {
        onClickButtonEvent(viewBinding.boldButton, ::isBoldState)
        onClickButtonEvent(viewBinding.italicButton, ::isItalicState)
        onClickButtonEvent(viewBinding.underlineButton, ::isUnderlineState)
        onClickButtonEvent(viewBinding.strikethroughButton, ::isStrikethroughState)
        viewBinding.fontSizeButton.setOnClickListener {
            showChangeTextSizeDialog()
        }
        viewBinding.colorTextButton.setOnClickListener {
            val colorForegroundSelected = FormattingBarFunctions().getForegroundColorText(
                viewBinding.editTextContent.text,
                lastStartSelection,
                lastEndSelection
            )
            openDialogColorForSpannable(
                ::colorText,
                ::isChangedColorText,
                viewBinding.colorTextButton,
                colorForegroundSelected
            )
        }
        viewBinding.colorBackgroundButton.setOnClickListener {
            val colorBackgroundSelected = FormattingBarFunctions().getForegroundColorText(
                viewBinding.editTextContent.text,
                lastStartSelection,
                lastEndSelection
            )
            openDialogColorForSpannable(
                ::backgroundColor,
                ::isChangedBackgroundText,
                viewBinding.colorBackgroundButton,
                colorBackgroundSelected
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openDialogColorForSpannable(
        colorString: KMutableProperty0<String>,
        state: KMutableProperty0<Boolean>,
        view: View,
        colorSelectedSpan: Int
    ) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutPickColorFormattingBarBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        //Init List Color
        val colors = listOf(
            "#000000",
            "#444444",
            "#888888",
            "#cdcdcd",
            "#ffffff",
            "#fe0000",
            "#01ff02",
            "#0000fe",
            "#ffff00",
            "#00ffff",
            "#ff01ff",
            "#fe0000",
            "#fe1d01",
            "#ff3900",
            "#ff5700",
            "#ff7300",
            "#ff9000",
            "#ffad01",
            "#ffcb00",
            "#ffe601",
            "#f9ff01",
            "#dffe02",
            "#bfff00",
            "#a4ff01",
            "#87ff00",
            "#69ff01",
            "#4cff00",
            "#30ff00",
            "#13ff00",
            "#01ff0c",
            "#00ff27",
            "#00ff43",
            "#00ff61",
            "#00ff7d",
            "#00fe9b",
            "#01ffb7",
            "#00ffd7",
            "#01fff2",
            "#01f1fe",
            "#01d4ff",
            "#00b6ff",
            "#0099ff",
            "#007dfe",
            "#0060ff",
            "#0043fe",
            "#0027ff",
            "#000aff",
            "#1400ff",
            "#3000fe",
            "#4d00fe",
            "#6a00ff",
            "#8800ff",
            "#a501ff",
            "#bf00fe",
            "#dd00fe",
            "#fa00ff",
            "#f923e5",
            "#fe00cb",
            "#ff02ad",
            "#ff0090",
            "#fe0072",
            "#fd0156",
            "#ff003c",
            "#ff011d"
        )
        //Binding Color Text
        if (lastStartSelection == lastEndSelection) {
            if (colorString.get() != "") {
                binding.txtSelectColor.setBackgroundColor(Color.parseColor(colorString.get()))
                val progress = getOpacityFromColor(Color.parseColor(colorString.get()))
                binding.seekOpacity.progress = progress
            } else {
                binding.txtSelectColor.setBackgroundColor(Color.TRANSPARENT)
            }
        } else {
            binding.txtSelectColor.setBackgroundColor(colorSelectedSpan)
            if (colorSelectedSpan != Color.TRANSPARENT) {
                val progress = getOpacityFromColor(colorSelectedSpan)
                binding.seekOpacity.progress = progress
            }
        }
        binding.recyclerViewPickColor.setHasFixedSize(false)
        val layoutManager = GridLayoutManager(requireContext(), 8)
        binding.recyclerViewPickColor.layoutManager = layoutManager
        var colorForSelectedSpan = colorSelectedSpan
        val adapter = AdapterForPickColor(colors, object : InterfaceOnClickListener {
            override fun onClickItemNoteListener(note: Note) {
            }

            override fun onClickColorItem(color: String) {
                binding.txtSelectColor.setBackgroundColor(Color.parseColor(color))
                if (lastStartSelection == lastEndSelection) {
                    colorString.set(color)
                } else {
                    colorForSelectedSpan = Color.parseColor(color)
                }
            }

            override fun onSelectedNote(listNoteSelectedResult: ArrayList<Note>) {
            }

            override fun onClickCategoriesItem(categories: Categories) {
            }
        })
        binding.recyclerViewPickColor.adapter = adapter
        binding.textViewOpacity.text =
            resources.getString(R.string.opacity) + "(${binding.seekOpacity.progress})"
        binding.seekOpacity.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n", "Range")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewOpacity.text =
                    resources.getString(R.string.opacity) + "(${binding.seekOpacity.progress})"
                val alpha = (progress * 255) / 100
                val colorWithAlpha: Int
                if (lastStartSelection == lastEndSelection) {
                    colorWithAlpha =
                        ColorUtils.setAlphaComponent(Color.parseColor(colorString.get()), alpha)
                    binding.txtSelectColor.setBackgroundColor(colorWithAlpha)
                    colorString.set(
                        getColorStringWithAlpha(
                            Color.parseColor(colorString.get()),
                            progress
                        )
                    )
                } else {
                    colorWithAlpha =
                        ColorUtils.setAlphaComponent(colorForSelectedSpan, alpha)
                    binding.txtSelectColor.setBackgroundColor(colorWithAlpha)
                    colorForSelectedSpan = colorWithAlpha
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.btnRemoveColor.setOnClickListener {
            if (lastStartSelection == lastEndSelection) {
                colorString.set("")
            } else {
                colorForSelectedSpan = 0
            }
            binding.txtSelectColor.setBackgroundColor(Color.WHITE)
        }
        binding.btnOk.setOnClickListener {
            if (lastEndSelection != lastStartSelection) {
                val editable = viewBinding.editTextContent.text
                if (view == viewBinding.colorBackgroundButton) {
                    val backgroundSpans = editable.getSpans(
                        lastStartSelection,
                        lastEndSelection,
                        BackgroundColorSpan::class.java
                    )
                    if (colorForSelectedSpan != 0) {
                        for (span in backgroundSpans) {
                            editable.removeSpan(span)
                        }
                        FormattingBarFunctions().applyBackgroundColor(
                            viewBinding.editTextContent,
                            lastStartSelection,
                            lastEndSelection,
                            colorForSelectedSpan
                        )
                        viewBinding.colorBackgroundButton.setBackgroundColor(
                            colorForSelectedSpan
                        )
                    } else {
                        for (span in backgroundSpans) {
                            editable.removeSpan(span)
                        }
                        viewBinding.colorBackgroundButton.setBackgroundColor(
                            Color.parseColor(
                                colorInstant
                            )
                        )
                    }
                } else if (view == viewBinding.colorTextButton) {
                    val colorSpans = editable.getSpans(
                        lastStartSelection,
                        lastEndSelection,
                        ForegroundColorSpan::class.java
                    )
                    if (colorForSelectedSpan != 0) {
                        for (span in colorSpans) {
                            editable.removeSpan(span)
                        }
                        FormattingBarFunctions().applyForegroundColor(
                            viewBinding.editTextContent,
                            lastStartSelection,
                            lastEndSelection,
                            colorForSelectedSpan
                        )
                        viewBinding.colorTextButton.setBackgroundColor(
                            colorForSelectedSpan

                        )
                    } else {
                        for (span in colorSpans) {
                            editable.removeSpan(span)
                        }
                        (viewBinding.colorTextButton.setBackgroundColor(
                            Color.parseColor(
                                colorInstant
                            )
                        ))
                    }
                }
            } else {
                if (colorString.get() != "") {
                    startIndex = viewBinding.editTextContent.text.length
                    state.set(true)
                    view.setBackgroundColor(Color.parseColor(colorString.get()))
                } else {
                    state.set(false)
                    view.setBackgroundColor(Color.parseColor(colorInstant))
                }
            }
            dialog.cancel()
        }
        dialog.show()
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun getColorStringWithAlpha(color: Int, opacity: Int): String {
        val alpha = (opacity * 255) / 100
        val colorWithAlpha = ColorUtils.setAlphaComponent(color, alpha)
        return String.format("#%08X", colorWithAlpha) // Returns #AARRGGBB
    }

    private fun getOpacityFromColor(color: Int): Int {
        val alpha = color.alpha
        val opacity = (alpha * 100) / 255
        return opacity
    }

    private fun onClickButtonEvent(button: ImageButton, state: KMutableProperty0<Boolean>) {
        //KMutableProperty0 <Boolean> is a bool variable that can be changed inside another function
        button.setOnClickListener {
            val editable = viewBinding.editTextContent.text
            if (lastStartSelection == lastEndSelection) {
                val newState = !state.get() //Inverse value
                state.set(newState) //Set new value
                setUpBackgroundForItemInFormattingBar(button, newState)
                if (newState) {
                    startIndex = viewBinding.editTextContent.text.length
                }
            } else {
                when (button) {
                    viewBinding.boldButton -> {
                        val boldSpans = editable.getSpans(
                            lastStartSelection,
                            lastEndSelection,
                            StyleSpan::class.java
                        )
                        val isBold = boldSpans.any { it.style == Typeface.BOLD }
                        if (!isBold) {
                            FormattingBarFunctions().applyBold(
                                viewBinding.editTextContent,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(viewBinding.boldButton, true)
                        } else {
                            FormattingBarFunctions().removeSpan(
                                editable,
                                StyleSpan::class.java,
                                Typeface.BOLD,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(viewBinding.boldButton, false)
                        }
                    }

                    viewBinding.italicButton -> {
                        val italicSpans = editable.getSpans(
                            lastStartSelection,
                            lastEndSelection,
                            StyleSpan::class.java
                        )
                        val isItalic = italicSpans.any { it.style == Typeface.ITALIC }
                        if (!isItalic) {
                            FormattingBarFunctions().applyItalic(
                                viewBinding.editTextContent,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(viewBinding.italicButton, true)
                        } else {
                            FormattingBarFunctions().removeSpan(
                                editable,
                                StyleSpan::class.java,
                                Typeface.ITALIC,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(viewBinding.italicButton, false)
                        }
                    }

                    viewBinding.underlineButton -> {
                        val underlineSpans = editable.getSpans(
                            lastStartSelection,
                            lastEndSelection,
                            UnderlineSpan::class.java
                        )
                        val isUnderline = underlineSpans.isNotEmpty()
                        if (!isUnderline) {
                            FormattingBarFunctions().applyUnderline(
                                viewBinding.editTextContent,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(viewBinding.underlineButton, true)
                        } else {
                            for (span in underlineSpans) {
                                editable.removeSpan(span)
                            }
                            setUpBackgroundForItemInFormattingBar(
                                viewBinding.underlineButton,
                                false
                            )
                        }
                    }

                    viewBinding.strikethroughButton -> {
                        val strikethroughSpans = editable.getSpans(
                            lastStartSelection,
                            lastEndSelection,
                            StrikethroughSpan::class.java
                        )
                        val isStrikethrough = strikethroughSpans.isNotEmpty()
                        if (!isStrikethrough) {
                            FormattingBarFunctions().applyStrikethrough(
                                viewBinding.editTextContent,
                                lastStartSelection,
                                lastEndSelection
                            )
                            setUpBackgroundForItemInFormattingBar(
                                viewBinding.strikethroughButton,
                                true
                            )
                        } else {
                            for (span in strikethroughSpans) {
                                editable.removeSpan(span)
                            }
                            setUpBackgroundForItemInFormattingBar(
                                viewBinding.strikethroughButton,
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setUpBackgroundForItemInFormattingBar(view: View, status: Boolean) {
        if (!status) {
            view.setBackgroundColor(Color.parseColor(colorInstant))
        } else {
            if (colorInstant != "#ffffdd") {
                view.setBackgroundColor(makeColorDarker(Color.parseColor(colorInstant)))
            } else {
                view.setBackgroundColor(resources.getColor(R.color.colorBackgroundButton))
            }
        }
    }

    private fun handleSavingData() {
        when (type) {
            "Create" -> createNote()
            "Update" -> updateNote()
        }
    }
    private fun updateNote() {
        val titleValue = viewBinding.editTextTitle.text.toString().trim()
        val contentValue = viewBinding.editTextContent.text.toString().trim()

        if (titleValue != note?.title || contentValue != note?.content || note?.spannableString != getSpannableStringFromEditText(viewBinding.editTextContent)  || note?.isBold != isBoldState || note?.isUnderline != isUnderlineState || note?.isItalic != isItalicState || note?.isStrikethrough != isStrikethroughState || note?.textSize != textSizeValue || note?.backgroundColorText != backgroundColor || note?.foregroundColorText != colorText
        ) {
            val spannableStringResult = getSpannableStringFromEditText(viewBinding.editTextContent)
            val dateString = getCurrentDateString()

            note?.apply {
                title = titleValue
                content = contentValue
                editedDate = dateString
                label = when {
                    titleValue.isEmpty() && contentValue.isEmpty() -> getString(R.string.Untitled)
                    titleValue.isEmpty() -> contentValue
                    else -> titleValue
                }
                spannableString = spannableStringResult
                isBold = isBoldState
                isItalic = isItalicState
                isUnderline = isUnderlineState
                isStrikethrough = isStrikethroughState
                textSize = textSizeValue
                foregroundColorText = colorText
                backgroundColorText = backgroundColor
            }
            note?.let { noteDatabase.noteDao().updateNote(it) }
            Toast.makeText(requireContext(), getString(R.string.save), Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createNote() {
        val title = viewBinding.editTextTitle.text.toString().trim().ifEmpty { "" }
        val content = viewBinding.editTextContent.text.toString().trim().ifEmpty { "" }
        val spannableString = getSpannableStringFromEditText(viewBinding.editTextContent)
        val dateString = getCurrentDateString()
        val noteModel = Note(
            title = title,
            content = content,
            createDate = dateString,
            editedDate = dateString,
        )
        noteModel.color = if (colorInstant != "#ffffdd") colorInstant else ""
        noteModel.label = when {
            title.isEmpty() && content.isEmpty() -> getString(R.string.Untitled)
            title.isEmpty() -> content
            else -> title
        }
        noteModel.spannableString = spannableString
        noteModel.isBold = isBoldState
        noteModel.isItalic = isItalicState
        noteModel.isUnderline = isUnderlineState
        noteModel.isStrikethrough = isStrikethroughState
        noteModel.textSize = textSizeValue
        noteModel.foregroundColorText = colorText
        noteModel.backgroundColorText = backgroundColor
        GlobalScope.launch(Dispatchers.IO) {
            val insertNoteId = noteDatabase.noteDao().insertNoteAndGetId(noteModel)
            categoriesInsertForNote?.let {
                val crossRef = NoteCategoryRef(insertNoteId.toInt(), it.idCategory!!)
                noteDatabase.noteDao().insertNoteCategoryCrossRef(crossRef)
            }
        }
        isCreatedData = true
        Toast.makeText(requireContext(), getString(R.string.save), Toast.LENGTH_SHORT).show()
    }

    private fun getSpannableStringFromEditText(editText: EditText): String {
        val spannableContent = editText.text as Spannable
        Log.d("Spannable to HTML", spannableContent.toString())
        val html = FormatString().spannableToHtml(spannableContent).trim()
        Log.d("Converted HTML", html)
        return html
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun bindData() {
        viewBinding.editTextTitle.setText(note?.title.toString())
        val spannableContent = FormatString().htmlToSpannable(note?.spannableString!!).trim()
        viewBinding.editTextContent.setText(spannableContent)
        if (note?.color != "") {
            colorInstant = note?.color!!
            viewBinding.linearLayoutDetailNote.setBackgroundColor(Color.parseColor(note?.color))
            viewBinding.layoutTitle.background =
                ColorDrawable(makeColorDarker(Color.parseColor(note?.color)))
        } else {
            viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
            viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
        note?.let {
            setupFormattingButtons(it)
            isChangedTextSize = it.textSize != 18
            textSizeValue = it.textSize
            if (isChangedTextSize) {
                startIndex = viewBinding.editTextContent.text.length
            }
            setUpBackgroundForItemInFormattingBar(viewBinding.fontSizeButton, isChangedTextSize)
            //Foreground Color Text
            bindColor(::isChangedColorText, it, viewBinding.colorTextButton, ::colorText)
            //Background Color Text
            bindColor(
                ::isChangedBackgroundText,
                it,
                viewBinding.colorBackgroundButton,
                ::backgroundColor
            )
        }
    }

    private fun bindColor(
        stateChangeColor: KMutableProperty0<Boolean>,
        note: Note,
        view: View,
        color: KMutableProperty0<String>
    ) {
        when (view) {
            viewBinding.colorTextButton -> {
                val bool = note.foregroundColorText != ""
                color.set(note.foregroundColorText)
                stateChangeColor.set(bool)
            }

            viewBinding.colorBackgroundButton -> {
                val bool = note.backgroundColorText != ""
                color.set(note.backgroundColorText)
                stateChangeColor.set(bool)
            }
        }
        if (stateChangeColor.get()) {
            startIndex = viewBinding.editTextContent.text.length
            view.setBackgroundColor(Color.parseColor(color.get()))
        } else view.setBackgroundColor(Color.parseColor(colorInstant))
    }

    private fun setupFormattingButtons(note: Note) {
        val formattingStates = listOf(
            Triple(note.isBold, ::isBoldState, viewBinding.boldButton),
            Triple(note.isItalic, ::isItalicState, viewBinding.italicButton),
            Triple(note.isUnderline, ::isUnderlineState, viewBinding.underlineButton),
            Triple(note.isStrikethrough, ::isStrikethroughState, viewBinding.strikethroughButton)
        )

        formattingStates.forEach { (state, formatFlag, button) ->
            formatFlag.set(state)
            setUpBackgroundForItemInFormattingBar(button, state)
            if (state) {
                startIndex = viewBinding.editTextContent.text.length
            }
        }
    }

    private fun handleColorTitleLayout(linearLayout: LinearLayout) {
        if (note?.color != "") {
            linearLayout.background = ColorDrawable(makeColorDarker(Color.parseColor(note?.color)))
        } else {
            linearLayout.setBackgroundColor(resources.getColor(R.color.theme_background))
        }
    }

    //Make color background more deeper
    private fun makeColorDarker(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.4f
        return Color.HSVToColor(hsv)
    }

    private fun initOriginalValueForEditText() {
        rootValue = viewBinding.editTextContent.text.toString()
    }

    private fun handleEditTextContent() {
        val textWatcher = object : TextWatcher {
            private var previousText: Spannable? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Save the current text before it changes
                previousText = if (s is Spannable) {
                    SpannableStringBuilder(s)
                } else {
                    SpannableStringBuilder.valueOf(s)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the undo button color if there is a change
                if (s.toString().trim() != rootValue) {
                    viewBinding.btnUndo.setTextColor(resources.getColor(R.color.color1))
                    viewBinding.btnUndo.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    // Add the previous text to the stack only if the current text is different than the older
                    if (s.toString() != previousText.toString() && !isChangingCharacter) {
                        undoStack.push(previousText)
                    }
                    if (isBoldState || isItalicState || isUnderlineState || isStrikethroughState || isChangedColorText || isChangedBackgroundText
                        || isChangedTextSize) {
                        val start = startIndex
                        val end = s.length
                        if (start < end) {
                            removeTextWatcher()
                            applyFormatting(start, end)
                            addTextWatcher()
                            startIndex = end
                        }
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

    private fun applyFormatting(start: Int, end: Int) {
        if (isBoldState) {
            FormattingBarFunctions().applyBold(
                viewBinding.editTextContent,
                start,
                end
            )
        }
        if (isItalicState) {
            FormattingBarFunctions().applyItalic(
                viewBinding.editTextContent,
                start,
                end
            )
        }
        if (isUnderlineState) {
            FormattingBarFunctions().applyUnderline(
                viewBinding.editTextContent,
                start,
                end
            )
        }
        if (isStrikethroughState) {
            FormattingBarFunctions().applyStrikethrough(
                viewBinding.editTextContent,
                start,
                end
            )
        }
        if(isChangedTextSize){
            FormattingBarFunctions().applyTextSize(viewBinding.editTextContent, start, end, textSizeValue)
        }
        if (isChangedColorText) {
            FormattingBarFunctions().applyForegroundColor(
                viewBinding.editTextContent,
                start,
                end,
                Color.parseColor(colorText)
            )
        }
        if (isChangedBackgroundText) {
            FormattingBarFunctions().applyBackgroundColor(
                viewBinding.editTextContent,
                start,
                end,
                Color.parseColor(backgroundColor)
            )
        }
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
                viewBinding.btnUndo.isEnabled = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showChangeTextSizeDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutDialogChangeTextSizeBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.seekBarTextSize.progress = textSizeValue
        binding.textSizeTitle.text = "Text Size ${textSizeValue}"
        binding.seekBarTextSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textSizeTitle.textSize = progress.toFloat()
                binding.textSizeTitle.text = "Text Size $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.buttonSetDefault.setOnClickListener {
            binding.seekBarTextSize.progress = 18
            binding.textSizeTitle.textSize = 18f
            binding.textSizeTitle.text = getString(R.string.text_size_18)
        }
        binding.btnCancelTextSize.setOnClickListener {
            dialog.cancel()
        }
        binding.btnOkTextSize.setOnClickListener {
            val selectedSize = binding.seekBarTextSize.progress
            isChangedTextSize = selectedSize != 18
            textSizeValue = selectedSize
            setUpBackgroundForItemInFormattingBar(viewBinding.fontSizeButton, isChangedTextSize)
            if (isChangedTextSize) {
                startIndex = viewBinding.editTextContent.text.length
            }
            dialog.cancel()
        }
        dialog.show()
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
        if (rootValue == "" || rootValue == viewBinding.editTextContent.text.toString()) {
            undoAllMenuItem.isEnabled = false
            undoAllMenuItem.title = undoAllMenuItem.title.toString().apply {
                undoAllMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.GRAY), 0, length, 0)
                }
            }
        } else {
            undoAllMenuItem.isEnabled = true
            undoAllMenuItem.title = undoAllMenuItem.title.toString().apply {
                undoAllMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.BLACK), 0, length, 0)
                }
            }
        }
        //Handle formatting bar item
        val formattingBarMenuItem = popupMenu.menu.findItem(R.id.open_formatting_bar)
        if (preferences.getStatusFormattingBar()) {
            formattingBarMenuItem.isEnabled = false
            formattingBarMenuItem.title = formattingBarMenuItem.title.toString().apply {
                formattingBarMenuItem.title = SpannableString(this).apply {
                    setSpan(ForegroundColorSpan(Color.BLACK), 0, length, 0)
                }
            }
        } else {
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
        } else {
            viewBinding.layoutTitleReadMode.visibility = View.GONE
            viewBinding.layoutTitle.visibility = View.VISIBLE
        }
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_delete, R.id.action_delete_read_mode -> {
                showDialogConfirm()
            }

            R.id.action_Categories, R.id.action_Categories_read_mode -> {
                showDialogCategory()
            }

            R.id.action_colorize, R.id.action_colorize_read_mode -> {
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

            R.id.show_info, R.id.show_info_read_mode -> {
                showDialogInfo()
            }

            R.id.undoAll -> {
                showUndoAllDialog()
            }

            R.id.exportTxtFiles -> {
                importExportManager.openDocumentTreeForNote(note)
            }

            R.id.switchToReadMode -> {
                readMode = true
                setReadOnlyMode()
            }

            R.id.share_menu -> {
                openIntentForShareOutSide()
            }

            R.id.open_formatting_bar -> {
                viewBinding.linearLayoutFormatingBar.visibility = View.VISIBLE
                preferences.putStatusFormattingBar(true)
            }
        }
    }

    private fun openIntentForShareOutSide() {
        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, viewBinding.editTextContent.text.toString())
        sendIntent.setType("text/plain")
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_note))
        startActivity(shareIntent)
    }

    private fun handleSearchInNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                FormattingBarFunctions().highlightMatches(
                    p0.toString(),
                    viewBinding.editTextContent
                )
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
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
    private fun createNoteCategoryLinks(listCategoriesInitial: ArrayList<Categories>) {
        val noteDao = noteDatabase.noteDao()
        if (listCategoriesSelected.size != 0) {
            GlobalScope.launch(Dispatchers.IO) {
                for (categoryInitial in listCategoriesInitial) {
                    if (!listCategoriesSelected.contains(categoryInitial)) {
                        val crossRef =
                            NoteCategoryRef(note!!.idNote!!, categoryInitial.idCategory!!)
                        noteDao.deleteNoteCategoryCrossRef(crossRef)
                    }
                }
                for (category in listCategoriesSelected) {
                    if (note != null) {
                        val linksExist = noteDatabase.noteDao()
                            .checkNoteCategoryRefExists(note!!.idNote!!, category.idCategory!!)
                        if (linksExist == 0) {
                            val crossRef = NoteCategoryRef(note!!.idNote!!, category.idCategory!!)
                            noteDao.insertNoteCategoryCrossRef(crossRef)
                        }
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.updated_categorize),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showUndoAllDialog() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.confirm))
            .setMessage(resources.getString(R.string.message_for_changes_note))
            .setPositiveButton(resources.getString(R.string.undo_all)) { dialogInterface, _ ->
                viewBinding.editTextContent.setText(note?.content)
                viewBinding.editTextContent.setSelection(note?.content.toString().length)
                dialogInterface.cancel()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    private fun showDialogConfirm() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage("${getString(R.string.want_to_remove)} ${note?.label}?")
            .setPositiveButton(getString(R.string.Yes)) { dialogInterface, _ ->
                note?.let { it ->
                    if (preferences.getStatusTrashValues()) {
                        it.isDelete = true
                        noteDatabase.noteDao().updateNote(it)
                    } else {
                        noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(it.idNote!!)
                        noteDatabase.noteDao().delete(it)
                    }
                }
                dialogInterface.cancel()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .setNegativeButton(getString(R.string.No)) { dialogInterface, _ ->
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
        binding.words.text =
            requireContext().getString(R.string.words) + getNumberOfWords(viewBinding.editTextContent.text.toString()).toString()
        binding.lineCount.text =
            requireContext().getString(R.string.line_count) + viewBinding.editTextContent.lineCount.toString()
        binding.characters.text =
            requireContext().getString(R.string.characters) + viewBinding.editTextContent.text.toString().length
        binding.charactersWithoutWhiteSpaces.text =
            requireContext().getString(R.string.characters_without_whitespaces) + getNumberOfCharactersWithoutSpace(
                viewBinding.editTextContent.text.toString()
            ).toString()
        binding.createdAt.text = requireContext().getString(R.string.created_at) + note?.createDate
        binding.LastSaved.text =
            requireContext().getString(R.string.last_saved_at) + note?.editedDate
        binding.btnOk.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun getNumberOfWords(input: String): Int {
        val wordCount = input.split("\\s+".toRegex()).size
        return wordCount
    }

    private fun getNumberOfCharactersWithoutSpace(input: String): Int {
        var result = 0
        val splitResult = input.split(" ")
        for (words in splitResult) {
            result += words.length
        }
        return result
    }

    private fun showDialogPickColor() {
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
        if (colorInstant != "#ffffdd") {
            binding.txtSelectColor.setBackgroundColor(Color.parseColor(colorInstant))
        } else {
            binding.txtSelectColor.setBackgroundColor(Color.WHITE)
        }
        //Init List Color
        val colors = listOf(
            "#ffadaf", "#ffd7a6", "#fdffb6", "#cbffc1", "#9cf6ff", "#a1c3ff",
            "#bdb2ff", "#ffc7ff",
            "#fffffb", "#d6e6ff", "#d7f9f8", "#ffffea", "#fff0d3", "#fcdfe1",
            "#e4d4ef", "#809cce", "#94b8d0", "#b8e0d5", "#d6eadf", "#eac3d5", "#8c5193", "#be4f91",
            "#fe6361", "#ff8530", "#fea601", "#ffd380"
        )
        binding.recyclerViewPickColor.setHasFixedSize(false)
        val layoutManager = GridLayoutManager(requireContext(), 6)
        binding.recyclerViewPickColor.layoutManager = layoutManager
        val adapter = AdapterForPickColor(colors, object : InterfaceOnClickListener {
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
            colorInstant = "#ffffdd"
            binding.txtSelectColor.setBackgroundColor(Color.WHITE)
        }
        binding.btnOk.setOnClickListener {
            if (colorInstant != "#ffffdd") {
                if (note != null) {
                    note?.color = colorInstant
                    note?.let { noteDatabase.noteDao().updateNote(it) }
                    viewBinding.linearLayoutDetailNote.setBackgroundColor(
                        Color.parseColor(
                            colorInstant
                        )
                    )
                    viewBinding.layoutTitle.background =
                        ColorDrawable(makeColorDarker(Color.parseColor(colorInstant)))
                } else {
                    viewBinding.linearLayoutDetailNote.setBackgroundColor(
                        Color.parseColor(
                            colorInstant
                        )
                    )
                    viewBinding.layoutTitle.background =
                        ColorDrawable(makeColorDarker(Color.parseColor(colorInstant)))
                }
            } else {
                if (note != null) {
                    note?.color = ""
                    note?.let { noteDatabase.noteDao().updateNote(it) }
                    viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
                    viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
                } else {
                    viewBinding.linearLayoutDetailNote.setBackgroundColor(resources.getColor(R.color.colorItem))
                    viewBinding.layoutTitle.setBackgroundColor(resources.getColor(R.color.theme_background))
                }
            }
            setUpBackgroundForItemInFormattingBar(viewBinding.boldButton, isBoldState)
            setUpBackgroundForItemInFormattingBar(viewBinding.italicButton, isItalicState)
            setUpBackgroundForItemInFormattingBar(viewBinding.underlineButton, isUnderlineState)
            setUpBackgroundForItemInFormattingBar(
                viewBinding.strikethroughButton,
                isStrikethroughState
            )
            setUpBackgroundForItemInFormattingBar(viewBinding.fontSizeButton, isChangedTextSize)
            if (colorText == "") viewBinding.colorTextButton.setBackgroundColor(
                Color.parseColor(
                    colorInstant
                )
            )
            if (backgroundColor == "") viewBinding.colorBackgroundButton.setBackgroundColor(
                Color.parseColor(
                    colorInstant
                )
            )
            dialog.cancel()
        }
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }
}