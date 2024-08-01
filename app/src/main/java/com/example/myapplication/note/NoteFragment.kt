package com.example.myapplication.note

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterRecyclerViewNote
import com.example.myapplication.databinding.CustomLayoutDialogSortBinding
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.menu_bar.ChangeFragmentFunctions
import com.example.myapplication.menu_bar.MenuBarFunction
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.note_view_model.NoteViewModel
import com.example.myapplication.note.options.ImportExportManager
import com.example.myapplication.preferences.NoteStatusPreferences
import com.google.android.material.navigation.NavigationView

@SuppressLint("NotifyDataSetChanged")
class NoteFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding: LayoutNoteFragmentBinding
    private lateinit var listNote: ArrayList<Note>
    private var listNoteSelected: ArrayList<Note> = ArrayList()
    private lateinit var adapter: AdapterRecyclerViewNote
    private lateinit var viewModel: NoteViewModel
    private lateinit var preferences: NoteStatusPreferences
    private lateinit var noteDatabase: NoteDatabase
    private var statusSort: String? = null
    private var listCategoriesForMenuBar: ArrayList<Categories> = ArrayList()
    private var category: Categories? = null
    private var type: String = "All"
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importExportManager: ImportExportManager
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        preferences = NoteStatusPreferences(requireContext())
        noteDatabase = NoteDatabase.getInstance(requireContext())
        statusSort = preferences.getStatusSortValues()
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    importExportManager.readNoteFromFile(uri, object : InterfaceCompleteListener{
                        override fun onCompleteListener(note: Note) {
                            noteDatabase.noteDao().insertNote(note)
                            Toast.makeText(
                                context,
                                "${note.label} ${getString(R.string.file_has_imported)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onEditCategoriesListener(categories: Categories) {

                        }

                        override fun onDeleteCategoriesListener(categories: Categories) {

                        }

                    })
                }
            }
        }
        exportLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    importExportManager.saveListNoteToDocument(uri, listNote)
                }
            }
        }
        importExportManager = ImportExportManager(requireActivity(), requireContext())
        importExportManager.setImportLauncher(importLauncher)
        importExportManager.setExportLauncher(exportLauncher)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutNoteFragmentBinding.inflate(inflater, container, false)
        viewBinding.searchButton.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.GONE
            viewBinding.layoutSearchToolBar.visibility = View.VISIBLE
        }
        viewBinding.back.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.VISIBLE
            viewBinding.layoutSearchToolBar.visibility = View.GONE
            viewBinding.searchBar.text.clear()
        }
        viewBinding.btnOpenClose.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(viewBinding.navView)
        }
        viewBinding.navView.setNavigationItemSelectedListener(this)
        viewBinding.btnCreate.setOnClickListener {
            changeToCreateNoteFragment()
        }
        viewBinding.txtSort.setOnClickListener {
            openDialogForSorting()
        }
        viewBinding.optionButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
        viewBinding.backFromSelectedNote.setOnClickListener {
            viewBinding.layoutSelectedNote.visibility = View.GONE
            viewBinding.layoutMainToolBar.visibility = View.VISIBLE
            adapter.onAllUnSelected()
        }
        viewBinding.checkBox.setOnClickListener {
            if (viewBinding.checkBox.isChecked) {
                adapter.onAllSelected()
            } else {
                adapter.onAllUnSelected()
            }
        }
        viewBinding.bin.setOnClickListener {
            showDialogConfirmDelete()
        }
        setUpRecyclerView()
        getDataFromCategories()
        getCategoryData()
        searchNote()
        return viewBinding.root
    }

    private fun getDataFromCategories() {
        val bundle = arguments
        if (bundle != null) {
            if (bundle["category"] != null) {
                category = bundle["category"] as Categories
                viewBinding.contentTxt.text = category!!.nameCategories
                viewBinding.contentTxt.visibility = View.VISIBLE
            }
            if (bundle["Type"] != null) {
                type = bundle["Type"] as String
            }
        }
        getNoteData()
    }

    private fun getCategoryData() {
        noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                listCategoriesForMenuBar.addAll(it)
                MenuBarFunction().loadCategoriesToMenu(it, viewBinding.navView.menu, resources)
            }
        }
    }

    private fun showDialogConfirmDelete() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.message_confirm_remove_notes))
            .setPositiveButton(getString(R.string.Yes)) { dialogInterface, _ ->
                for (note in listNoteSelected) {
                    if (preferences.getStatusTrashValues()) {
                        note.isDelete = true
                        noteDatabase.noteDao().updateNote(note)
                    } else {
                        noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(note.idNote!!)
                        noteDatabase.noteDao().delete(note)
                    }
                }
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.delete)} ${listNoteSelected.size} ${getString(R.string.notes)}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                listNoteSelected.clear()
                viewBinding.layoutSelectedNote.visibility = View.GONE
                viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                adapter.isSelected = false
                adapter.clearListSelectedNote()
                dialogInterface.cancel()
            }
            .setNegativeButton(getString(R.string.No)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    private fun showPopupMenu(view: View?) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_options_note, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
            true
        }
        popupMenu.show()
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.exportNote -> {
                importExportManager.openDocumentTreeForListNote(listNote)
            }

            R.id.importNote -> {
                importExportManager.openDocumentPicker()
            }

            R.id.selectAllNote -> {
                adapter.onAllSelected()
            }
        }
    }

    private fun checkStatusSort() {
        if (statusSort != null) {
            when (statusSort) {
                getString(R.string.a_z) -> {
                    sortByAToZ()
                }

                getString(R.string.z_a) -> {
                    sortByZtoA()
                }

                getString(R.string.new_date_created) -> {
                    sortByNewestDate()
                }

                getString(R.string.old_date_created) -> {
                    sortByOldestDate()
                }

                getString(R.string.new_date_edited) -> {
                    sortByNewestEditDay()
                }

                getString(R.string.old_date_edited) -> {
                    sortByOldestEditDay()
                }
            }
        }
    }

    private fun searchNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.getFilter().filter(p0.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

    }

    private fun getNoteData() {
        viewModel.clearSources()
        if (category != null && type == "category") {
            val noteDAO = noteDatabase.noteDao()
            viewModel.getNoteByCategories(category!!.idCategory!!, noteDAO)
        } else if (type == "All") {
            viewModel.getLiveDataNote(requireContext())
        } else if (type == "Uncategorized") {
            viewModel.getUncategorizedNote(noteDatabase.noteDao())
        }
        viewModel.notes.observe(viewLifecycleOwner) { result ->
            handleListNote(result)
        }
    }

    private fun handleListNote(listNoteResult: List<Note>) {
        listNote.clear()
        for (note in listNoteResult) {
            if (!note.isDelete) {
                listNote.add(note)
                noteDatabase.noteDao().getNoteWithCategories(note.idNote!!)
                    .observe(viewLifecycleOwner) {
                        if (it != null && it.listCategories.isNotEmpty()) {
                            var categoriesString = ""
                            for (item in it.listCategories) {
                                if (it.listCategories.indexOf(item) == it.listCategories.size - 1) {
                                    categoriesString += item.nameCategories
                                } else {
                                    categoriesString += "${item.nameCategories}, "
                                }
                            }
                            note.listCategories = categoriesString
                            adapter.notifyItemChanged(listNoteResult.indexOf(note))
                        }
                    }
            }
        }
        adapter.notifyDataSetChanged()
        checkStatusSort()
    }

    private fun setUpRecyclerView() {
        viewBinding.recyclerViewNote.setHasFixedSize(false)
        listNote = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        adapter = AdapterRecyclerViewNote(listNote, object : InterfaceOnClickListener {
            override fun onClickItemNoteListener(note: Note) {
                changeToDetailNoteFragment(note)
            }

            override fun onClickColorItem(color: String) {

            }

            override fun onSelectedNote(listNoteSelectedResult: ArrayList<Note>) {
                if (listNoteSelectedResult.size == 0) {
                    viewBinding.layoutSelectedNote.visibility = View.GONE
                    viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                } else {
                    viewBinding.layoutSelectedNote.visibility = View.VISIBLE
                    viewBinding.layoutMainToolBar.visibility = View.GONE
                    viewBinding.layoutSearchToolBar.visibility = View.GONE
                    viewBinding.numberSelected.text = listNoteSelectedResult.size.toString()
                    listNoteSelected.clear()
                    listNoteSelected.addAll(listNoteSelectedResult)
                }
            }

            override fun onClickCategoriesItem(categories: Categories) {

            }
        }, requireContext())
        viewBinding.recyclerViewNote.layoutManager = linearLayout
        viewBinding.recyclerViewNote.adapter = adapter
    }

    private fun changeToCreateNoteFragment() {
        val detailFragment = DetailNoteFragment()
        if (category != null) {
            val bundle = Bundle()
            bundle.putSerializable("CategoryForNote", category)
            detailFragment.arguments = bundle
        }
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }

    private fun changeToDetailNoteFragment(note: Note) {
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("Note", note)
        detailFragment.arguments = bundle
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }

    private fun openDialogForSorting() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = CustomLayoutDialogSortBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.show()
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
        val status = preferences.getStatusSortValues()
        when (status) {
            getString(R.string.a_z) -> {
                binding.rbTitleAtoZ.isChecked = true
            }

            getString(R.string.z_a) -> {
                binding.rbTitleZtoA.isChecked = true
            }

            getString(R.string.new_date_created) -> {
                binding.rbFromNewestDate.isChecked = true
            }

            getString(R.string.old_date_created) -> {
                binding.rbFromOldestDate.isChecked = true
            }

            getString(R.string.new_date_edited) -> {
                binding.rbEditDateNewest.isChecked = true
            }

            getString(R.string.old_date_edited) -> {
                binding.rbEditDateOldest.isChecked = true
            }
        }
        binding.btnSort.setOnClickListener {
            if (binding.rbEditDateNewest.isChecked) {
                sortByNewestEditDay()
                preferences.putStatusSortValues(getString(R.string.new_date_edited))
            } else if (binding.rbEditDateOldest.isChecked) {
                sortByOldestEditDay()
                preferences.putStatusSortValues(getString(R.string.old_date_edited))
            } else if (binding.rbTitleAtoZ.isChecked) {
                sortByAToZ()
                preferences.putStatusSortValues(getString(R.string.a_z))
            } else if (binding.rbTitleZtoA.isChecked) {
                sortByZtoA()
                preferences.putStatusSortValues(getString(R.string.z_a))
            } else if (binding.rbFromNewestDate.isChecked) {
                sortByNewestDate()
                preferences.putStatusSortValues(getString(R.string.new_date_created))
            } else if (binding.rbFromOldestDate.isChecked) {
                sortByOldestDate()
                preferences.putStatusSortValues(getString(R.string.old_date_created))
            }
            dialog.cancel()
        }
    }

    private fun sortByOldestDate() {
        listNote.sortWith(Comparator { h1, h2 ->
            h1.createDate.compareTo(h2.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestDate() {
        listNote.sortWith(Comparator { h1, h2 ->
            h2.createDate.compareTo(h1.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByZtoA() {
        listNote.sortWith(Comparator { h1, h2 ->
            h2.label.lowercase().compareTo(h1.label.lowercase())
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByAToZ() {
        listNote.sortWith { h1, h2 ->
            h1.label.lowercase().compareTo(h2.label.lowercase())
        }
        adapter.notifyDataSetChanged()
    }

    private fun sortByOldestEditDay() {
        listNote.sortWith(Comparator { h1, h2 ->
            h1.editedDate.compareTo(h2.editedDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestEditDay() {
        listNote.sortWith(Comparator { h1, h2 ->
            h2.editedDate.compareTo(h1.editedDate)
        })
        adapter.notifyDataSetChanged()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.note -> {
                viewBinding.contentTxt.visibility = View.GONE
                category = null
                type = getString(R.string.all)
                getNoteData()
            }

            R.id.editCategories -> {
                ChangeFragmentFunctions(requireActivity()).onChangedToCategoriesFragment()
            }

            R.id.delete -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToTrashFragment()
            }

            R.id.setting -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToSettingFragment()
            }

            2311 -> {
                type = getString(R.string.uncategorized)
                viewBinding.contentTxt.visibility = View.VISIBLE
                viewBinding.contentTxt.text = type
                getNoteData()
            }

            else -> {
                val menu = viewBinding.navView.menu
                val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)
                if (categoriesGroupItem != null) {
                    val categoriesGroup = categoriesGroupItem.subMenu
                    categoriesGroup?.let {
                        if (it.findItem(item.itemId) != null && item.itemId != R.id.editCategories && item.itemId != 2311) {
                            val selectedCategory = listCategoriesForMenuBar.find { category ->
                                category.nameCategories == item.title
                            }
                            selectedCategory?.let {
                                //Handle action select category
                                category = selectedCategory
                                type = getString(R.string.category)
                                viewBinding.contentTxt.visibility = View.VISIBLE
                                viewBinding.contentTxt.text = category!!.nameCategories
                                getNoteData()
                            }
                        }
                    }
                }
            }
        }
        viewBinding.drawerLayout.closeDrawer(viewBinding.navView)
        return true
    }
}