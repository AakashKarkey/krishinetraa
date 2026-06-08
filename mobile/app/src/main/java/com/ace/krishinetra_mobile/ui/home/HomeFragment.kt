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

        binding.btnAnalyzePlant.setOnClickListener {
            findNavController().navigate(R.id.analyzeFragment)
        }

        binding.btnLearnMore.setOnClickListener {
            binding.nestedScrollView.post {
                binding.nestedScrollView.smoothScrollTo(0, binding.llHowItWorks.top)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
