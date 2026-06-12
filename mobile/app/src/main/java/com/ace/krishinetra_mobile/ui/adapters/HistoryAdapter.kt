package com.ace.krishinetra_mobile.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ace.krishinetra_mobile.data.model.AnalysisRecord
import com.ace.krishinetra_mobile.databinding.ItemHistoryRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (AnalysisRecord) -> Unit
) : ListAdapter<AnalysisRecord, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: AnalysisRecord) {
            binding.diseaseName.text = record.diseaseName
            binding.confidenceText.text = "${record.confidence}% confidence"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
            binding.dateText.text = dateFormat.format(Date(record.timestamp))

            when {
                record.diseaseName == "Healthy" -> {
                    binding.statusBadge.setBackgroundResource(com.ace.krishinetra_mobile.R.drawable.bg_badge_green)
                }
                record.diseaseName == "Unrecognizable" -> {
                    binding.statusBadge.setBackgroundResource(com.ace.krishinetra_mobile.R.drawable.bg_badge_gray)
                }
                else -> {
                    binding.statusBadge.setBackgroundResource(com.ace.krishinetra_mobile.R.drawable.bg_badge_red)
                }
            }

            binding.root.setOnClickListener { onItemClick(record) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AnalysisRecord>() {
        override fun areItemsTheSame(oldItem: AnalysisRecord, newItem: AnalysisRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AnalysisRecord, newItem: AnalysisRecord): Boolean {
            return oldItem == newItem
        }
    }
}
