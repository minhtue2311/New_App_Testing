package com.example.myapplication.trash

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
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
import com.example.myapplication.categories.CategoriesFragment
import com.example.myapplication.databinding.LayoutActionItemTrashBinding
import com.example.myapplication.databinding.LayoutTrashFragmentBinding
import com.example.myapplication.menu_bar.ChangeFragmentFunctions
import com.example.myapplication.menu_bar.MenuBarFunction
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.NoteFragment
import com.example.myapplication.note.note_view_model.NoteViewModel
import com.example.myapplication.note.options.ExportNote
import com.example.myapplication.note.options.ImportExportManager
import com.example.myapplication.setting.SettingFragment
import com.google.android.material.navigation.NavigationView

class TrashFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding: LayoutTrashFragmentBinding
    private var listCategories: ArrayList<Categories> = ArrayList()
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var viewModel: NoteViewModel
    private lateinit var listTrash: ArrayList<Note>
    private lateinit var adapter: AdapterRecyclerViewNote
    private lateinit var importExportManager: ImportExportManager
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private var listTrashSelected: ArrayList<Note> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        noteDatabase = NoteDatabase.getInstance(requireContext())
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        exportLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    importExportManager.saveListNoteToDocument(uri, listTrash)
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
        viewBinding = LayoutTrashFragmentBinding.inflate(inflater, container, false)
        viewBinding.btnOpenClose.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(viewBinding.navView)
        }
        viewBinding.navView.setNavigationItemSelectedListener(this)
        viewBinding.checkBox.setOnClickListener {
            if (viewBinding.checkBox.isChecked) {
                adapter.onAllSelected()
            } else {
                adapter.onAllUnSelected()
            }
        }
        viewBinding.bin.setOnClickListener {
            showDialogConfirmRestore()
        }
        viewBinding.checkBox.setOnClickListener {
            if (viewBinding.checkBox.isChecked) {
                adapter.onAllSelected()
            } else {
                adapter.onAllUnSelected()
            }
        }
        viewBinding.optionButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
        viewBinding.backFromSelectedNote.setOnClickListener {
            viewBinding.layoutMainToolBar.visibility = View.VISIBLE
            viewBinding.layoutSelectedNote.visibility = View.GONE
        }
        getCategoriesData()
        setUpRecyclerView()
        getTrashData()
        return viewBinding.root
    }


    private fun showPopupMenu(view: View?) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_options_item_trash, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
            true
        }
        popupMenu.show()
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.undeleteAll -> {
                restoreAllTrash()
            }

            R.id.export_trash -> {
                importExportManager.openDocumentTreeForListNote(listTrash)
            }

            R.id.clean -> {
                cleanAllTrash()
            }
        }
    }

    private fun cleanAllTrash() {
        showDialogConfirmClean()
    }

    private fun showDialogConfirmClean() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.title_confirm_remove_trash))
            .setPositiveButton(getString(R.string.Yes)) { dialogInterface, _ ->
                for (trash in listTrash) {
                    noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(trash.idNote!!)
                    noteDatabase.noteDao().delete(trash)
                }
                Toast.makeText(
                    requireContext(),
                    getString(R.string.delete_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.cancel()
            }
            .setNegativeButton(getString(R.string.No)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    private fun restoreTrash(trash: Note) {
        trash.isDelete = false
        noteDatabase.noteDao().updateNote(trash)
        Toast.makeText(requireContext(), getString(R.string.restore_one_note), Toast.LENGTH_SHORT)
            .show()
    }


    private fun restoreAllTrash() {
        for (trash in listTrash) {
            restoreTrash(trash)
        }
        Toast.makeText(requireContext(), "Restore ${listTrash.size} notes", Toast.LENGTH_SHORT)
            .show()
        listTrash.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showDialogConfirmRestore() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.title_confirm_restore_note))
            .setPositiveButton(getString(R.string.Yes)) { dialogInterface, _ ->
                for (trash in listTrashSelected) {
                    restoreTrash(trash)
                }
                Toast.makeText(
                    requireContext(),
                    "Restore ${listTrashSelected.size} notes",
                    Toast.LENGTH_SHORT
                ).show()
                listTrashSelected.clear()
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

    private fun setUpRecyclerView() {
        viewBinding.recyclerViewTrash.setHasFixedSize(false)
        listTrash = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        adapter = AdapterRecyclerViewNote(listTrash, object : InterfaceOnClickListener {
            override fun onClickItemNoteListener(note: Note) {
                openDialogForItemTrash(note)
            }

            override fun onClickColorItem(color: String) {

            }

            override fun onSelectedNote(listNoteSelectedResult: ArrayList<Note>) {
                if(listNoteSelectedResult.size == 0){
                    viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                    viewBinding.layoutSelectedNote.visibility = View.GONE
                }else{
                    viewBinding.layoutSelectedNote.visibility = View.VISIBLE
                    viewBinding.layoutMainToolBar.visibility = View.GONE
                    viewBinding.numberSelected.text = listTrash.size.toString()
                    listTrashSelected.clear()
                    listTrashSelected.addAll(listNoteSelectedResult)
                }
            }

            override fun onClickCategoriesItem(categories: Categories) {

            }
        }, requireContext())
        viewBinding.recyclerViewTrash.adapter = adapter
        viewBinding.recyclerViewTrash.layoutManager = linearLayout
    }

    private fun openDialogForItemTrash(trash: Note) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutActionItemTrashBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.show()
        binding.btnOk.setOnClickListener {
            if (binding.UndeleteItem.isChecked) {
                restoreTrash(trash)
            }
            if (binding.DeleteItem.isChecked) {
                noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(trash.idNote!!)
                noteDatabase.noteDao().delete(trash)
            }
            dialog.cancel()
        }
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun getCategoriesData() {
        noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                listCategories.addAll(it)
                MenuBarFunction().loadCategoriesToMenu(listCategories,viewBinding.navView.menu, resources)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getTrashData() {
        viewModel.getTrashNote(requireContext())
        viewModel.notes.observe(viewLifecycleOwner) { result ->
            listTrash.clear()
            listTrash.addAll(result)
            adapter.notifyDataSetChanged()
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.note -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToNoteFragment(getString(R.string.all))
            }

            R.id.editCategories -> {
                ChangeFragmentFunctions(requireActivity()).onChangedToCategoriesFragment()
            }

            R.id.delete -> {

            }

            R.id.setting -> {
               ChangeFragmentFunctions(requireActivity()).onChangeToSettingFragment()
            }

            2311 -> {
               ChangeFragmentFunctions(requireActivity()).onChangeToNoteFragment(getString(R.string.uncategorized))
            }

            else -> {
                val menu = viewBinding.navView.menu
                val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)
                if (categoriesGroupItem != null) {
                    val categoriesGroup = categoriesGroupItem.subMenu
                    categoriesGroup?.let {
                        if (it.findItem(item.itemId) != null && item.itemId != R.id.editCategories) {
                            val selectedCategory = listCategories.find { category ->
                                category.nameCategories == item.title
                            }
                            selectedCategory?.let {
                                //Handle action select category
                                ChangeFragmentFunctions(requireActivity()).onChangeToNoteFragmentWithCategories(selectedCategory)
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