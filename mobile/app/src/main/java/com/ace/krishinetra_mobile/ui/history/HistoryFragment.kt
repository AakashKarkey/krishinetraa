package com.ace.krishinetra_mobile.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentHistoryBinding
import com.ace.krishinetra_mobile.ui.adapters.HistoryAdapter
import com.ace.krishinetra_mobile.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = HistoryAdapter { record ->
            val bundle = Bundle().apply {
                putLong("record_id", record.id)
            }
            findNavController().navigate(R.id.action_history_to_detail, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.records.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records)
            if (records.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        binding.btnClearAll.setOnClickListener {
            viewModel.deleteAllRecords()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
