package com.example.myapplication.categories

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterListCategories
import com.example.myapplication.categories.categories_model.CategoriesViewModel
import com.example.myapplication.databinding.LayoutCategoriesBinding
import com.example.myapplication.databinding.LayoutEditCategoryNameBinding
import com.example.myapplication.menu_bar.ChangeFragmentFunctions
import com.example.myapplication.menu_bar.MenuBarFunction
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import com.example.myapplication.note.NoteFragment
import com.example.myapplication.setting.SettingFragment
import com.example.myapplication.trash.TrashFragment
import com.google.android.material.navigation.NavigationView

class CategoriesFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding: LayoutCategoriesBinding
    private lateinit var listCategories: ArrayList<Categories>
    private lateinit var adapter: AdapterListCategories
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var noteDatabase: NoteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        noteDatabase = NoteDatabase.getInstance(requireContext())
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutCategoriesBinding.inflate(inflater, container, false)
        viewBinding.btnOpenClose.setOnClickListener {
            viewBinding.drawerLayout.openDrawer(viewBinding.navView)
        }
        viewBinding.navView.setNavigationItemSelectedListener(this)
        viewBinding.btnAdd.setOnClickListener {
            if (viewBinding.editTextNewCategories.text.toString().isNotEmpty()) {
                val categories = Categories(null, viewBinding.editTextNewCategories.text.toString())
                addNewCategories(categories)
            }
        }
        setUpRecyclerView()
        getData()
        return viewBinding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addNewCategories(categories: Categories) {
        noteDatabase.categoriesDao().insertCategories(categories)
        listCategories.add(categories)
        adapter.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getData() {
        viewModel.getLiveDataCategories(requireContext()).observe(viewLifecycleOwner) {
            listCategories.clear()
            listCategories.addAll(it)
            MenuBarFunction().loadCategoriesToMenu(
                listCategories,
                viewBinding.navView.menu,
                resources
            )
            adapter.notifyDataSetChanged()
        }
    }

    private fun setUpRecyclerView() {
        viewBinding.recyclerViewCategories.setHasFixedSize(false)
        listCategories = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        viewBinding.recyclerViewCategories.layoutManager = linearLayout
        adapter = AdapterListCategories(listCategories, object : InterfaceCompleteListener {
            override fun onCompleteListener(note: Note) {
            }

            override fun onEditCategoriesListener(categories: Categories) {
                showDialogConfirmEdit(categories)
            }

            override fun onDeleteCategoriesListener(categories: Categories) {
                showDialogConfirmDelete(categories)
            }

        })
        viewBinding.recyclerViewCategories.adapter = adapter
    }

    private fun showDialogConfirmEdit(categories: Categories) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = LayoutEditCategoryNameBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        binding.editTxtCategory.setText(categories.nameCategories)
        binding.btnOk.setOnClickListener {
            categories.nameCategories = binding.editTxtCategory.text.toString()
            noteDatabase.categoriesDao().updateCategories(categories)
            dialog.cancel()
        }
        binding.btnCancel.setOnClickListener {
            dialog.cancel()
        }
        dialog.show()
    }

    private fun showDialogConfirmDelete(categories: Categories) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.title_confirm_remove_note))
            .setPositiveButton(getString(R.string.Yes)) { dialogInterface, _ ->
                noteDatabase.categoriesDao()
                    .deleteNoteCategoriesByCategoryId(categories.idCategory!!)
                noteDatabase.categoriesDao().deleteCategory(categories)
                dialogInterface.cancel()
            }
            .setNegativeButton(getString(R.string.No)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.note -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToNoteFragment(getString(R.string.all))
            }

            R.id.editCategories -> {

            }

            R.id.setting -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToSettingFragment()
            }

            R.id.delete -> {
                ChangeFragmentFunctions(requireActivity()).onChangeToTrashFragment()
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
                                ChangeFragmentFunctions(requireActivity()).onChangeToNoteFragmentWithCategories(
                                    selectedCategory
                                )
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