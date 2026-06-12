package com.ace.krishinetra_mobile.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentLearnBinding

class LearnFragment : Fragment() {
    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.cardEarlyBlight.setOnClickListener {
            val bundle = Bundle().apply {
                putString("disease_key", "Early Blight")
            }
            findNavController().navigate(R.id.action_learn_to_detail, bundle)
        }

        binding.cardLateBlight.setOnClickListener {
            val bundle = Bundle().apply {
                putString("disease_key", "Late Blight")
            }
            findNavController().navigate(R.id.action_learn_to_detail, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
