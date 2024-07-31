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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterRecyclerViewNote
import com.example.myapplication.categories.CategoriesFragment
import com.example.myapplication.databinding.LayoutActionItemTrashBinding
import com.example.myapplication.databinding.LayoutTrashFragmentBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.NoteFragment
import com.example.myapplication.note.note_view_model.NoteViewModel
import com.example.myapplication.note.options.ExportNote
import com.example.myapplication.setting.SettingFragment
import com.google.android.material.navigation.NavigationView

class TrashFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding: LayoutTrashFragmentBinding
    private var listCategories: ArrayList<Categories> = ArrayList()
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var viewModel: NoteViewModel
    private lateinit var listTrash: ArrayList<Note>
    private lateinit var adapter: AdapterRecyclerViewNote
    private var listTrashSelected: ArrayList<Note> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        noteDatabase = NoteDatabase.getInstance(requireContext())
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
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
                openDocumentTree()
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

    private fun openDocumentTree() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 1)
    }

    @Deprecated("Deprecated in Java")    //Tra ve ket qua la uri cua thu muc duoc chon
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                ExportNote(requireContext(), requireActivity()).saveListNoteToDocument(
                    uri,
                    listTrash
                )
            }
        }
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
                loadCategoriesToMenu(it)
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

    private fun loadCategoriesToMenu(it: List<Categories>?) {
        val menu = viewBinding.navView.menu
        val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)

        if (categoriesGroupItem != null) {
            val categoriesGroup = categoriesGroupItem.subMenu

            categoriesGroup?.let {
                it.clear()
                //Add Edit Categories Item Menu
                it.add(Menu.NONE, R.id.editCategories, Menu.NONE, getString(R.string.edit_categorized))
                    .setIcon(R.drawable.baseline_playlist_add_24)
                //Add Uncategorized Item Menu
                it.add(Menu.NONE, 2311, Menu.NONE, getString(R.string.uncategorized))
                    .setIcon(R.drawable.dont_tag)

                listCategories.forEachIndexed { index, category ->
                    val itemId = Menu.FIRST + index
                    it.add(R.id.categoriesGroup, itemId, Menu.NONE, category.nameCategories)
                        ?.setIcon(R.drawable.tag)
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.note -> {
                onChangeToNoteFragment("All")
            }

            R.id.editCategories -> {
                onChangedToCategoriesFragment()
            }

            R.id.delete -> {

            }

            R.id.setting -> {
                onChangeToSettingFragment()
            }

            2311 -> {
                onChangeToNoteFragment("Uncategorized")
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
                                onChangeToNoteFragmentWithCategories(selectedCategory)
                            }
                        }
                    }
                }
            }
        }
        viewBinding.drawerLayout.closeDrawer(viewBinding.navView)
        return true
    }

    private fun onChangeToNoteFragmentWithCategories(value: Categories) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putSerializable("category", value)
        bundle.putString("Type", "category")
        noteFragment.arguments = bundle
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }

    private fun onChangeToNoteFragment(value: String) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putString("Type", value)
        noteFragment.arguments = bundle
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }

    private fun onChangedToCategoriesFragment() {
        val categoriesFragment = CategoriesFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, categoriesFragment)
        fragmentTrans.commit()
    }

    private fun onChangeToSettingFragment() {
        val settingFragment = SettingFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, settingFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
}