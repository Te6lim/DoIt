package com.te6lim.doit.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.te6lim.doit.MainActivity
import com.te6lim.doit.R
import com.te6lim.doit.database.CategoryDb
import com.te6lim.doit.database.getInstance
import com.te6lim.doit.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var binding: FragmentSummaryBinding

    private lateinit var summaryViewModel: SummaryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_summary, container, false
        )

        mainActivity = (requireActivity() as MainActivity)

        summaryViewModel = ViewModelProvider(
            requireActivity(), SummaryViewModelFactory(
                getInstance(requireActivity()).summaryDao,
                CategoryDb.getInstance(requireContext()).dao
            )
        )[SummaryViewModel::class.java]

        binding.lifecycleOwner = this

        binding.viewModel = summaryViewModel

        with(summaryViewModel) {
            readySummary.observe(viewLifecycleOwner) { }
            categories.observe(viewLifecycleOwner) { }
        }

        binding.resetButton.setOnClickListener { summaryViewModel.resetSummary() }

        return binding.root
    }
}