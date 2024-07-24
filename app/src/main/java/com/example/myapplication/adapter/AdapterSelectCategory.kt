package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.LayoutCustomItemSelectCategoryBinding
import com.example.myapplication.model.Categories

class AdapterSelectCategory(
    private var listCategories: ArrayList<Categories>,
    private var listCategoriesSelected: ArrayList<Categories>,
) : RecyclerView.Adapter<AdapterSelectCategory.ViewHolderSelectCategory>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderSelectCategory {
        val viewBinding = LayoutCustomItemSelectCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderSelectCategory(viewBinding)
    }

    override fun getItemCount(): Int {
        return listCategories.size
    }

    override fun onBindViewHolder(holder: ViewHolderSelectCategory, position: Int) {
        val model = listCategories[position]
        holder.bindData(model)

        // Kiểm tra xem model này có thuộc listCategoriesSelected không
        holder.getViewBinding().checkBoxSelected.isChecked = listCategoriesSelected.contains(model)

        holder.getViewBinding().checkBoxSelected.setOnClickListener {
            if (holder.getViewBinding().checkBoxSelected.isChecked) {
                // Nếu checkbox được chọn, thêm model vào listCategoriesSelected
                if (!listCategoriesSelected.contains(model)) {
                    listCategoriesSelected.add(model)
                }
            } else {
                // Nếu checkbox bị bỏ chọn, loại bỏ model khỏi listCategoriesSelected
                listCategoriesSelected.remove(model)
            }
        }
    }

    class ViewHolderSelectCategory(private var viewBinding: LayoutCustomItemSelectCategoryBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bindData(categories: Categories) {
            viewBinding.titleCategories.text = categories.nameCategories
        }

        fun getViewBinding(): LayoutCustomItemSelectCategoryBinding {
            return viewBinding
        }
    }
}
