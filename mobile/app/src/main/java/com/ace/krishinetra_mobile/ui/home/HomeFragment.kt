package com.ace.krishinetra_mobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentHomeBinding
import com.ace.krishinetra_mobile.viewmodel.HomeViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardAnalyze.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_analyze)
        }

        binding.cardChat.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_chat)
        }

        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.cardLearn.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_learn)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
