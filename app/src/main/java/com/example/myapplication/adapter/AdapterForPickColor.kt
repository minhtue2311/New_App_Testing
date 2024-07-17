package com.example.myapplication.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.LayoutEachColorBinding
import com.example.myapplication.model.interface_model.InterfaceOnClickListener

class AdapterForPickColor(private var listColor : List<String>, private var onColorClickListener : InterfaceOnClickListener) : RecyclerView.Adapter<ColorViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val viewBinding = LayoutEachColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
       return listColor.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bindData(listColor[position])
        holder.getView().colorView.setOnClickListener {
            onColorClickListener.onClickColorItem(listColor[position])
        }
    }

}
class ColorViewHolder(private var viewBinding: LayoutEachColorBinding) : RecyclerView.ViewHolder(viewBinding.root){
    fun bindData(color : String){
        viewBinding.colorView.setBackgroundColor(Color.parseColor(color))
    }
    fun getView() : LayoutEachColorBinding{
        return viewBinding
    }
}