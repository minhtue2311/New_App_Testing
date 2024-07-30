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
import android.text.TextWatcher
import android.util.Log
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
import com.example.myapplication.databinding.CustomLayoutDialogSortBinding
import com.example.myapplication.databinding.LayoutNoteFragmentBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.Note
import com.example.myapplication.model.NoteDatabase
import com.example.myapplication.model.Trash
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import com.example.myapplication.model.interface_model.InterfaceOnClickListener
import com.example.myapplication.note.options.ExportNote
import com.example.myapplication.note.noteViewModel.NoteViewModel
import com.example.myapplication.note.options.ImportNote
import com.example.myapplication.preferences.NoteStatusPreferences
import com.example.myapplication.trash.TrashFragment
import com.google.android.material.navigation.NavigationView

@SuppressLint("NotifyDataSetChanged")
class NoteFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewBinding : LayoutNoteFragmentBinding
    private lateinit var listNote : ArrayList<Note>
    private var listNoteSelected : ArrayList<Note> = ArrayList()
    private lateinit var adapter : AdapterRecyclerViewNote
    private lateinit var viewModel : NoteViewModel
    private lateinit var preferences : NoteStatusPreferences
    private lateinit var noteDatabase: NoteDatabase
    private var statusSort : String? = null
    private var listCategoriesForMenuBar : ArrayList<Categories> = ArrayList()
    private var category : Categories? = null
    private var type : String = "All"
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        preferences = NoteStatusPreferences(requireContext())
        noteDatabase = NoteDatabase.getInstance(requireContext())
        statusSort = preferences.getStatusSortValues()
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
        viewBinding.btnCreate.setOnClickListener{
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
            if(viewBinding.checkBox.isChecked){
                adapter.onAllSelected()
            }else{
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
        if(bundle != null){
            if(bundle["category"] != null){
                category = bundle["category"] as Categories
                viewBinding.contentTxt.text = category!!.nameCategories
                viewBinding.contentTxt.visibility = View.VISIBLE
            }
            if(bundle["Type"] != null){
                type = bundle["Type"] as String
            }
        }
        getNoteData()
    }

    private fun getCategoryData() {
        noteDatabase.categoriesDao().getAllCategories().observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                listCategoriesForMenuBar.addAll(it)
                loadCategoriesToMenu(it)
            }
        }
    }
    private fun showDialogConfirmDelete() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Confirm")
            .setMessage("Do you want to remove this selected notes ?")
            .setPositiveButton("Yes") { dialogInterface, _ ->
                for (note in listNoteSelected) {
                    noteDatabase.noteDao().deleteNoteCategoriesRefByNoteId(note.idNote!!)
                   note.let {
                       val trashModel = Trash(
                           idNoteTrash = null,
                           title = it.title,
                           content = it.content,
                           createDate = it.createDate,
                           editedDate = it.editedDate,
                           color = it.color,
                           label = it.label,
                           spannableString = it.spannableString,
                           isBold = it.isBold,
                           isItalic = it.isItalic,
                           isUnderline = it.isUnderline,
                           isStrikethrough = it.isStrikethrough,
                           textSize = it.textSize,
                           foregroundColorText = it.foregroundColorText,
                           backgroundColorText = it.foregroundColorText,
                       )
                       noteDatabase.noteDao().moveNoteToTrash(trashModel)
                   }
                    noteDatabase.noteDao().delete(note)
                }
                Toast.makeText(requireContext(), "Delete ${listNoteSelected.size} notes", Toast.LENGTH_SHORT)
                    .show()
                listNoteSelected.clear()
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
                openDocumentTree()
            }
            R.id.importNote -> {
                openDocumentPicker()
            }
            R.id.selectAllNote ->{
                adapter.onAllSelected()
            }
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        startActivityForResult(intent, 2)
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
                ExportNote(requireContext(), requireActivity()).saveListNoteToDocument(uri, listNote)
            }
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                ImportNote(requireActivity(), requireContext(), object : InterfaceCompleteListener{
                    override fun onCompleteListener(note: Note) {
                        noteDatabase.noteDao().insertNote(note)
                        Toast.makeText(requireContext(),"${note.label} file has imported !", Toast.LENGTH_SHORT).show()
                    }

                    override fun onEditCategoriesListener(categories: Categories) {

                    }

                    override fun onDeleteCategoriesListener(categories: Categories) {

                    }
                }).readNoteFromFile(uri)
            }
        }
    }

    private fun checkStatusSort() {
        if(statusSort != null){
            when(statusSort){
                "A->Z" -> {
                    sortByAToZ()
                }
                "Z->A" -> {
                    sortByZtoA()
                }
                "NewestDateCreated" -> {sortByNewestDate()}
                "OldestDateCreated" -> {sortByOldestDate()}
                "NewestDateEdited" -> {sortByNewestEditDay()}
                "OldestDateEdited" ->{sortByOldestEditDay()}
            }
        }
    }

    private fun searchNote() {
        viewBinding.searchBar.addTextChangedListener(object : TextWatcher{
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

    private fun getNoteData(){
        viewModel.clearSources()
        if(category != null && type == "category"){
            val noteDAO = noteDatabase.noteDao()
            viewModel.getNoteByCategories(category!!.idCategory!!, noteDAO)
        }else if(type == "All"){
            viewModel.getLiveDataNote(requireContext())
        }else if(type == "Uncategorized"){
            viewModel.getUncategorizedNote(noteDatabase.noteDao())
        }
        viewModel.notes.observe(viewLifecycleOwner){result ->
            handleListNote(result)
        }
    }
    private fun handleListNote(listNoteResult: List<Note>) {
        listNote.clear()
        for (note in listNoteResult) {
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
        adapter.notifyDataSetChanged()
        checkStatusSort()
    }


    private fun setUpRecyclerView() {
        viewBinding.recyclerViewNote.setHasFixedSize(false)
        listNote = ArrayList()
        val linearLayout = LinearLayoutManager(requireContext())
        adapter = AdapterRecyclerViewNote(listNote, object : InterfaceOnClickListener{
            override fun onClickItemNoteListener(note: Note) {
                changeToDetailNoteFragment(note)
            }

            override fun onClickColorItem(color: String) {

            }

            override fun onSelectedNote(listNoteSelectedResult: ArrayList<Note>) {
                if(listNoteSelectedResult.size == 0){
                    viewBinding.layoutSelectedNote.visibility = View.GONE
                    viewBinding.layoutMainToolBar.visibility = View.VISIBLE
                }else {
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

    private fun changeToCreateNoteFragment(){
        val detailFragment = DetailNoteFragment()
        if(category != null){
            val bundle = Bundle()
            bundle.putSerializable("CategoryForNote", category)
            detailFragment.arguments = bundle
        }
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
    private fun changeToDetailNoteFragment(note : Note){
        val detailFragment = DetailNoteFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("Note", note)
        detailFragment.arguments = bundle
        fragmentTrans.add(R.id.mainLayout, detailFragment)
        fragmentTrans.addToBackStack(null)
        fragmentTrans.commit()
    }
    private fun openDialogForSorting(){
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
        when(status){
            "A->Z" -> {binding.rbTitleAtoZ.isChecked = true}
            "Z->A" -> {binding.rbTitleZtoA.isChecked = true}
            "NewestDateCreated" -> {binding.rbFromNewestDate.isChecked = true}
            "OldestDateCreated" -> {binding.rbFromOldestDate.isChecked = true}
            "NewestDateEdited" -> {binding.rbEditDateNewest.isChecked = true}
            "OldestDateEdited" ->{binding.rbEditDateOldest.isChecked = true}
        }
        binding.btnSort.setOnClickListener {
            if(binding.rbEditDateNewest.isChecked){
                sortByNewestEditDay()
                preferences.putStatusSortValues("NewestDateEdited")
            }else if(binding.rbEditDateOldest.isChecked){
                sortByOldestEditDay()
                preferences.putStatusSortValues("OldestDateEdited")
            }else if(binding.rbTitleAtoZ.isChecked){
                sortByAToZ()
                preferences.putStatusSortValues("A->Z")
            }else if(binding.rbTitleZtoA.isChecked){
                sortByZtoA()
                preferences.putStatusSortValues("Z->A")
            }else if(binding.rbFromNewestDate.isChecked){
                sortByNewestDate()
                preferences.putStatusSortValues("NewestDateCreated")
            }else if(binding.rbFromOldestDate.isChecked){
                sortByOldestDate()
                preferences.putStatusSortValues("OldestDateCreated")
            }
            dialog.cancel()
        }
    }
    private fun sortByOldestDate() {
        listNote.sortWith(Comparator{h1,h2 ->
            h1.createDate.compareTo(h2.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestDate() {
        listNote.sortWith(Comparator{h1,h2 ->
            h2.createDate.compareTo(h1.createDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByZtoA() {
        listNote.sortWith(Comparator{h1,h2 ->
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
        listNote.sortWith(Comparator{h1,h2 ->
            h1.editedDate.compareTo(h2.editedDate)
        })
        adapter.notifyDataSetChanged()
    }

    private fun sortByNewestEditDay() {
        listNote.sortWith(Comparator{h1,h2 ->
            h2.editedDate.compareTo(h1.editedDate)
        })
        adapter.notifyDataSetChanged()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.note -> {
                viewBinding.contentTxt.visibility = View.GONE
                category = null
                type = "All"
                getNoteData()
            }
            R.id.editCategories -> {
                onChangedToCategoriesFragment()
            }
            R.id.delete ->{
                onChangeToTrashFragment()
            }
            2311 -> {
                type = "Uncategorized"
                viewBinding.contentTxt.visibility = View.VISIBLE
                viewBinding.contentTxt.text = type
                getNoteData()
            }
            else ->{
                val menu = viewBinding.navView.menu
                val categoriesGroupItem = menu.findItem(R.id.categoriesGroup)
                if (categoriesGroupItem != null) {
                    val categoriesGroup = categoriesGroupItem.subMenu
                    categoriesGroup?.let {
                        if (it.findItem(item.itemId) != null && item.itemId != R.id.editCategories && item.itemId!= 2311) {
                            val selectedCategory = listCategoriesForMenuBar.find { category ->
                                category.nameCategories == item.title
                            }
                            selectedCategory?.let {
                                //Handle action select category
                                category = selectedCategory
                                type = "category"
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

    private fun onChangeToTrashFragment() {
        val trashFragment = TrashFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, trashFragment)
        fragmentTrans.commit()
    }

    private fun onChangedToCategoriesFragment() {
        val categoriesFragment = CategoriesFragment()
        val fragmentTrans = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.mainLayout, categoriesFragment)
        fragmentTrans.commit()
    }
    private fun loadCategoriesToMenu(listCategories: List<Categories>) {
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
}