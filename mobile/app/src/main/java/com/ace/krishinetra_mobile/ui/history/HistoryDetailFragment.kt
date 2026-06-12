package com.ace.krishinetra_mobile.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ace.krishinetra_mobile.R
import com.ace.krishinetra_mobile.data.local.AppDatabase
import com.ace.krishinetra_mobile.databinding.FragmentHistoryDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryDetailFragment : Fragment() {
    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnClickListener {
            findNavController().navigateUp()
        }

        val recordId = arguments?.getLong("record_id") ?: return

        scope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).analysisDao()
            val allRecords = mutableListOf<com.ace.krishinetra_mobile.data.model.AnalysisRecord>()
            dao.getAllRecords().collect { list ->
                if (allRecords.isEmpty()) {
                    allRecords.addAll(list)
                    val record = list.find { it.id == recordId }
                    if (record != null) {
                        launch(Dispatchers.Main) { bindRecord(record) }
                    }
                }
            }
        }
    }

    private fun bindRecord(record: com.ace.krishinetra_mobile.data.model.AnalysisRecord) {
        binding.diseaseName.text = record.diseaseName
        binding.confidenceText.text = "${record.confidence}% confidence"

        val dateFormat = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
        binding.dateText.text = dateFormat.format(Date(record.timestamp))

        if (record.message != null) {
            binding.sectionMessage.visibility = View.VISIBLE
            binding.messageText.text = record.message
            binding.descriptionSection.visibility = View.GONE
            binding.treatmentSection.visibility = View.GONE
            binding.preventionSection.visibility = View.GONE
        } else {
            binding.sectionMessage.visibility = View.GONE
            binding.descriptionText.text = record.description
            binding.treatmentText.text = record.treatment

            binding.preventionList.removeAllViews()
            val tips = record.preventionTips.split("\n")
            for (tip in tips) {
                val itemView = layoutInflater.inflate(
                    R.layout.item_prevention_tip, binding.preventionList, false
                )
                val tipText = itemView.findViewById<android.widget.TextView>(R.id.tipText)
                tipText.text = tip.trim()
                binding.preventionList.addView(itemView)
            }
        }

        if (record.greenRatio != null) {
            binding.greenRatioText.text = "Green pixel ratio: ${"%.0f".format(record.greenRatio * 100)}%"
            binding.greenRatioSection.visibility = View.VISIBLE
        } else {
            binding.greenRatioSection.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
