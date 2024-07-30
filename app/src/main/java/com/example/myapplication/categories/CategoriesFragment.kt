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
import com.example.myapplication.categories.categoriesModel.CategoriesViewModel
import com.example.myapplication.databinding.LayoutCategoriesBinding
import com.example.myapplication.databinding.LayoutEditCategoryNameBinding
import com.example.myapplication.databinding.LayoutShowInfoNoteBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import com.example.myapplication.note.NoteFragment
import com.example.myapplication.trash.TrashFragment
import com.google.android.material.navigation.NavigationView

class CategoriesFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding : LayoutCategoriesBinding
    private lateinit var listCategories: ArrayList<Categories>
    private lateinit var adapter : AdapterListCategories
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
            if(viewBinding.editTextNewCategories.text.toString().isNotEmpty()){
                val categories = Categories(null,viewBinding.editTextNewCategories.text.toString())
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
        viewModel.getLiveDataCategories(requireContext()).observe(viewLifecycleOwner){
            listCategories.clear()
            listCategories.addAll(it)
            loadCategoriesToMenu(listCategories)
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadCategoriesToMenu(listCategories: ArrayList<Categories>) {
        val menu = viewBinding.navView.menu
        val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)

        if (categoriesGroupItem != null) {
            val categoriesGroup = categoriesGroupItem.subMenu

            // Xóa các phần tử động trước đó, giữ lại phần tử "Edit categories"
            categoriesGroup?.let {
                it.clear()
                it.add(Menu.NONE, R.id.editCategories, Menu.NONE, "Edit categories")
                    .setIcon(R.drawable.baseline_playlist_add_24)
                it.add(Menu.NONE, 2311, Menu.NONE, "Uncategorized")
                    .setIcon(R.drawable.dont_tag)
                listCategories.forEachIndexed { index, category ->
                    val itemId = Menu.FIRST + index
                    it.add(R.id.categoriesGroup, itemId, Menu.NONE, category.nameCategories)
                        ?.setIcon(R.drawable.tag)
                }
            }
        } else {
            Log.e("CategoriesFragment", "categoriesGroupItem is null")
        }
    }
    private fun setUpRecyclerView() {
        viewBinding.recyclerViewCategories.setHasFixedSize(false)
        listCategories = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        viewBinding.recyclerViewCategories.layoutManager = linearLayout
        adapter = AdapterListCategories(listCategories, object : InterfaceCompleteListener{
            override fun onCompleteListener(note: Note) {
            }

            override fun onEditCategoriesListener(categories: Categories) {
               showDialogConfirmEdit(categories)
            }

            override fun onDeleteCategoriesListener(categories: Categories) {
                showDialogConfirmDelete(categories)
            }

        })
        viewBinding.recyclerViewCategories.adapter= adapter
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
            .setTitle("Confirm")
            .setMessage("Do you want to remove this selected notes? Notes from the category won't be deleted ?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                noteDatabase.categoriesDao().deleteNoteCategoriesByCategoryId(categories.idCategory!!)
               noteDatabase.categoriesDao().deleteCategory(categories)
                dialogInterface.cancel()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.note -> {
                onChangeToNoteFragment("All")
            }
            R.id.editCategories -> {

            }
            R.id.delete -> {
                onChangeToTrashFragment()
            }
            2311 ->{
                onChangeToNoteFragment("Uncategorized")
            }
            else ->{
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

    private fun onChangeToTrashFragment() {
        val trashFragment = TrashFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, trashFragment)
        fragmentTrans.commit()
    }

    private fun onChangeToNoteFragmentWithCategories(value : Categories) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putSerializable("category", value)
        bundle.putString("Type", "category")
        noteFragment.arguments = bundle
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }
    private fun onChangeToNoteFragment(value : String) {
        val noteFragment = NoteFragment()
        val bundle = Bundle()
        bundle.putString("Type", value)
        noteFragment.arguments = bundle
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }
}