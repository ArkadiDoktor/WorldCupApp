package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.R
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.databinding.ItemMatchBinding
import com.worldcup.app.utils.formatMatchDate
import com.worldcup.app.utils.getCountryFlag

class MatchesAdapter(
    private val onPredictClick: ((MatchEntity) -> Unit)? = null
) : ListAdapter<MatchEntity, MatchesAdapter.MatchViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MatchEntity>() {
            override fun areItemsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MatchEntity, newItem: MatchEntity) =
                oldItem == newItem
        }
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: MatchEntity) {
            binding.apply {
                tvHomeTeam.text = match.homeTeamName ?: itemView.context.getString(R.string.tbd)
                tvAwayTeam.text = match.awayTeamName ?: itemView.context.getString(R.string.tbd)
                tvHomeFlag.text = getCountryFlag(match.homeTeamName)
                tvAwayFlag.text = getCountryFlag(match.awayTeamName)
                tvMatchDate.text = match.utcDate.formatMatchDate()
                tvGroup.text = match.group ?: match.stage.replace("_", " ")

                when (match.status) {
                    "FINISHED" -> {
                        tvScore.text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                        tvStatus.text = itemView.context.getString(R.string.status_finished)
                        tvStatus.setTextColor(itemView.context.getColor(R.color.status_finished))
                    }
                    "IN_PLAY", "PAUSED" -> {
                        tvScore.text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}"
                        tvStatus.text = itemView.context.getString(R.string.status_live)
                        tvStatus.setTextColor(itemView.context.getColor(R.color.status_live))
                    }
                    else -> {
                        tvScore.text = "vs"
                        tvStatus.text = itemView.context.getString(R.string.status_scheduled)
                        tvStatus.setTextColor(itemView.context.getColor(R.color.status_scheduled))
                    }
                }

                // Only allow predictions for matches that haven't started yet —
                // once a match is live (IN_PLAY/PAUSED) or finished, the outcome
                // is already determined or in progress, so predicting is pointless.
                val matchNotYetStarted = match.status != "FINISHED" &&
                    match.status != "IN_PLAY" &&
                    match.status != "PAUSED"

                if (onPredictClick != null) {
                    btnPredict.visibility = android.view.View.VISIBLE
                    btnPredict.text = when (match.status) {
                        "FINISHED" -> "View Predictions"
                        "IN_PLAY", "PAUSED" -> "Live Predictions"
                        else -> "Predict Score"
                    }
                    btnPredict.setOnClickListener { onPredictClick.invoke(match) }
                } else {
                    btnPredict.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
