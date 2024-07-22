package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.CustomItemCategoriesBinding
import com.example.myapplication.model.Categories
import com.example.myapplication.model.interface_model.InterfaceCompleteListener
import com.example.myapplication.model.interface_model.InterfaceOnClickListener

class AdapterListCategories(private var listCategories : ArrayList<Categories>, private var onClickListener: InterfaceCompleteListener) : RecyclerView.Adapter<ViewHolderCategories>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCategories {
        val viewBinding = CustomItemCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderCategories(viewBinding)
    }

    override fun getItemCount(): Int {
        return listCategories.size
    }

    override fun onBindViewHolder(holder: ViewHolderCategories, position: Int) {
        val model = listCategories[position]
        holder.bindData(model)
        holder.getViewBinding().btnEdit.setOnClickListener {
            onClickListener.onEditCategoriesListener(model)
        }
        holder.getViewBinding().btnDelete.setOnClickListener {
            onClickListener.onDeleteCategoriesListener(model)
        }
    }
}
class ViewHolderCategories(private var viewBinding : CustomItemCategoriesBinding) : RecyclerView.ViewHolder(viewBinding.root){
    fun bindData(categories: Categories){
        viewBinding.valueTag.text = categories.nameCategories
    }
    fun getViewBinding() : CustomItemCategoriesBinding{
        return viewBinding
    }
}