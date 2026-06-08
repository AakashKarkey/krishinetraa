package com.ace.krishinetra_mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ace.krishinetra_mobile.R
import java.text.DecimalFormat

class ProbabilityAdapter(
    private val probabilities: Map<String, Double>
) : RecyclerView.Adapter<ProbabilityAdapter.ProbabilityViewHolder>() {

    private val items: List<Pair<String, Double>> = probabilities.entries
        .sortedByDescending { it.value }
        .map { it.key to it.value }

    private val decimalFormat = DecimalFormat("#.#")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProbabilityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_probability, parent, false)
        return ProbabilityViewHolder(view, decimalFormat)
    }

    override fun onBindViewHolder(holder: ProbabilityViewHolder, position: Int) {
        val (label, probability) = items[position]
        holder.bind(label, probability)
    }

    override fun getItemCount(): Int = items.size

    class ProbabilityViewHolder(itemView: View, private val decimalFormat: DecimalFormat) : RecyclerView.ViewHolder(itemView) {
        private val labelText: TextView = itemView.findViewById(R.id.labelText)
        private val percentText: TextView = itemView.findViewById(R.id.percentText)
        private val fillBar: View = itemView.findViewById(R.id.fillBar)

        fun bind(label: String, probability: Double) {
            labelText.text = label
            val percent = if (probability <= 1.0) probability * 100 else probability
            percentText.text = "${decimalFormat.format(percent)}%"

            val fillWidth = (percent / 100.0).toFloat().coerceIn(0f, 1f)
            fillBar.post {
                val parentWidth = (fillBar.parent as View).width
                fillBar.layoutParams.width = (parentWidth * fillWidth).toInt()
                fillBar.requestLayout()
            }
        }
    }
}
