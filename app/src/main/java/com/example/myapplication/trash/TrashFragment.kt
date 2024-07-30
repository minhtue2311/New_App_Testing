package com.example.myapplication.trash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AdapterTrash
import com.example.myapplication.categories.CategoriesFragment
import com.example.myapplication.databinding.LayoutTrashFragmentBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.Trash
import com.example.myapplication.note.NoteFragment
import com.google.android.material.navigation.NavigationView

class TrashFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding : LayoutTrashFragmentBinding
    private lateinit var listCategories: ArrayList<Categories>
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var viewModel : TrashViewModel
    private lateinit var listTrash : ArrayList<Trash>
    private lateinit var adapter : AdapterTrash
    private var listTrashSelected : ArrayList<Trash> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        noteDatabase = NoteDatabase.getInstance(requireContext())
        viewModel = ViewModelProvider(this)[TrashViewModel::class.java]
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
        getCategoriesData()
        setUpRecyclerView()
        getTrashData()
        return viewBinding.root
    }

    override fun onResume() {
        viewBinding.bin.setOnClickListener {
            showDialogConfirmRestore()
        }
        super.onResume()
    }

    private fun showDialogConfirmRestore() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Confirm")
            .setMessage("Do you want to restore this selected notes ?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                for (trash in listTrash) {
                    trash.let {
                        val note = Note(
                            title = it.title,
                            content = it.content,
                            createDate = it.createDate,
                            editedDate = it.editedDate,
                        )
                        note.label = trash.label
                        note.color =trash.color
                        note.spannableString = trash.spannableString
                        note.isBold = trash.isBold
                        note.isItalic = trash.isItalic
                        note.isUnderline = trash.isUnderline
                        note.isStrikethrough = trash.isStrikethrough
                        note.textSize = trash.textSize
                        note.foregroundColorText = trash.foregroundColorText
                        note.backgroundColorText = trash.backgroundColorText
                        noteDatabase.noteDao().insertNote(note)
                    }
                    noteDatabase.noteDao().deleteTrash(trash)
                }
                Toast.makeText(requireContext(), "Delete Successfully", Toast.LENGTH_SHORT)
                    .show()
                listTrash.clear()
                viewBinding.layoutSelectedNote.visibility = View.GONE
                viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                adapter.isSelected = false
                adapter.clearListSelectedNote()
                dialogInterface.cancel()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        dialog.show()
    }

    private fun setUpRecyclerView() {
        viewBinding.recyclerViewTrash.setHasFixedSize(false)
        listTrash = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        adapter = AdapterTrash(listTrash, object : AdapterTrash.OnClickTrashListener{
            override fun onClickTrashListener(trash: Trash) {

            }

            override fun onLongClickTrashListener(listTrash: ArrayList<Trash>) {
                if(listTrash.size == 0){
                    viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                    viewBinding.layoutSelectedNote.visibility = View.GONE
                }else{
                    viewBinding.layoutSelectedNote.visibility = View.VISIBLE
                    viewBinding.layoutMainToolBar.visibility = View.GONE
                    viewBinding.numberSelected.text = listTrash.size.toString()
                    listTrashSelected.clear()
                    listTrashSelected.addAll(listTrash)
                }
            }

        }, requireContext())
        viewBinding.recyclerViewTrash.adapter = adapter
        viewBinding.recyclerViewTrash.layoutManager = linearLayout
    }
    private fun getCategoriesData() {
        noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                listCategories.addAll(it)
                loadCategoriesToMenu(it)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getTrashData(){
        viewModel.getLiveDataTrash(requireContext()).observe(viewLifecycleOwner){
            if(it != null){
                listTrash.clear()
                listTrash.addAll(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadCategoriesToMenu(it: List<Categories>?) {
        val menu = viewBinding.navView.menu
        val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)

        if (categoriesGroupItem != null) {
            val categoriesGroup = categoriesGroupItem.subMenu

            // Xóa các phần tử động trước đó, giữ lại phần tử "Edit categories"
            categoriesGroup?.let {
                val editCategoryItem = it.findItem(R.id.categories)
                it.clear()
                //Add Edit Categories Item Menu
                it.add(Menu.NONE, R.id.editCategories, Menu.NONE, "Edit categories")
                    .setIcon(R.drawable.baseline_playlist_add_24)
                //Add Uncategorized Item Menu
                it.add(Menu.NONE, 2311, Menu.NONE, "Uncategorized")
                    .setIcon(R.drawable.dont_tag)

                // Thêm các phần tử mới từ danh sách với ID duy nhất
                listCategories.forEachIndexed { index, category ->
                    val itemId = Menu.FIRST + index   // Sử dụng ID duy nhất cho mỗi phần tử, tránh trùng với R.id.categories
                    it.add(R.id.categoriesGroup, itemId, Menu.NONE, category.nameCategories)
                        ?.setIcon(R.drawable.tag)
                }
            }
        } else {
            // Xử lý nếu categoriesGroupItem bị null (có thể ghi log hoặc hiện thông báo lỗi)
            Log.e("CategoriesFragment", "categoriesGroupItem is null")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.note -> {
                onChangeToNoteFragment("All")
            }
            R.id.editCategories -> {
                onChangedToCategoriesFragment()
            }
            R.id.delete -> {

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
    private fun onChangedToCategoriesFragment() {
        val categoriesFragment = CategoriesFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, categoriesFragment)
        fragmentTrans.commit()
    }
}