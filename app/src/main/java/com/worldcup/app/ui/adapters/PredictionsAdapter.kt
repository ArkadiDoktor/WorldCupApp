package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.data.remote.firebase.Prediction
import com.worldcup.app.databinding.ItemPredictionBinding

class PredictionsAdapter : RecyclerView.Adapter<PredictionsAdapter.PredictionViewHolder>() {

    private var items: List<Prediction> = emptyList()

    fun submitList(newItems: List<Prediction>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class PredictionViewHolder(private val binding: ItemPredictionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(prediction: Prediction) {
            binding.tvUserName.text = prediction.userName
            binding.tvPredictedScore.text =
                "${prediction.predictedHomeScore} - ${prediction.predictedAwayScore}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val binding = ItemPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PredictionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
