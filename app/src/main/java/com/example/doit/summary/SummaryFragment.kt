package com.example.doit.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.doit.R
import com.example.doit.database.CategoryDb
import com.example.doit.database.TodoDatabase
import com.example.doit.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {

    private lateinit var binding: FragmentSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_summary, container, false
        )

        val catDb = CategoryDb.getInstance(requireContext()).dao
        val todoDb = TodoDatabase.getInstance(requireContext()).databaseDao

        val summaryViewModel = ViewModelProvider(
            this, SummaryViewModelFactory(catDb, todoDb)
        )[SummaryViewModel::class.java]

        binding.lifecycleOwner = this

        with(summaryViewModel) {

        }

        return binding.root
    }
}