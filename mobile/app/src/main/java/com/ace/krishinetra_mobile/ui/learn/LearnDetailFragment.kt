package com.ace.krishinetra_mobile.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.databinding.FragmentLearnDetailBinding
import com.ace.krishinetra_mobile.utils.Constants

class LearnDetailFragment : Fragment() {
    private var _binding: FragmentLearnDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnClickListener {
            findNavController().navigateUp()
        }

        val diseaseKey = arguments?.getString("disease_key") ?: return
        val diseaseInfo = Constants.Disease.DISEASE_INFO[diseaseKey]

        binding.diseaseTitle.text = diseaseKey

        if (diseaseInfo != null) {
            binding.sectionDescription.text = diseaseInfo.description
            binding.sectionTreatment.text = diseaseInfo.treatment

            binding.preventionList.removeAllViews()
            for (tip in diseaseInfo.preventionTips) {
                val itemView = layoutInflater.inflate(R.layout.item_prevention_tip, binding.preventionList, false)
                val bulletText = itemView.findViewById<android.widget.TextView>(R.id.tipText)
                bulletText.text = tip
                binding.preventionList.addView(itemView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
