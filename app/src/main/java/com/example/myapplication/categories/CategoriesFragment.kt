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
import com.google.android.material.navigation.NavigationView

class CategoriesFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding : LayoutCategoriesBinding
    private lateinit var listCategories: ArrayList<Categories>
    private lateinit var adapter : AdapterListCategories
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var noteDatabase: NoteDatabase
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutCategoriesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CategoriesViewModel::class.java]
        noteDatabase = NoteDatabase.getInstance(requireContext())

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

        // Kiểm tra nếu categoriesGroupItem không bị null
        if (categoriesGroupItem != null) {
            val categoriesGroup = categoriesGroupItem.subMenu
            categoriesGroup?.clear() // Xóa các phần tử cũ trước khi thêm mới
            listCategories.forEach { category ->
                categoriesGroup?.add(R.id.categoriesGroup, Menu.NONE, Menu.NONE, category.nameCategories)
                    ?.setIcon(R.drawable.tag)
            }
        } else {
            // Xử lý nếu categoriesGroupItem bị null (có thể ghi log hoặc hiện thông báo lỗi)
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
            .setMessage("Do you want to remove this selected notes ?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
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
                onChangeToNoteFragment()
            }
            R.id.categories -> {}
        }
        viewBinding.drawerLayout.closeDrawer(viewBinding.navView)
        return true
    }

    private fun onChangeToNoteFragment() {
        val noteFragment = NoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, noteFragment)
        fragmentTrans.commit()
    }

}