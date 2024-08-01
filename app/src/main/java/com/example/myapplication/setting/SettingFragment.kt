package com.example.myapplication.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.LayoutSettingBinding
import com.example.myapplication.preferences.NoteStatusPreferences

class SettingFragment : Fragment() {
    private lateinit var viewBinding : LayoutSettingBinding
    private lateinit var preferences: NoteStatusPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = NoteStatusPreferences(requireContext())
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = LayoutSettingBinding.inflate(inflater, container, false)
        handleStatusMoveToTrash()
        viewBinding.switchButton.setOnCheckedChangeListener { _, b ->
            if(b){
                preferences.putStatusTrashValues(true)
            }else{
                preferences.putStatusTrashValues(false)
            }
        }
        viewBinding.backFromSetting.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        viewBinding.layoutSetting.setOnClickListener {
            //TO DO
        }
        return viewBinding.root
    }
    private fun handleStatusMoveToTrash(){
        viewBinding.switchButton.isChecked = preferences.getStatusTrashValues()
    }
}