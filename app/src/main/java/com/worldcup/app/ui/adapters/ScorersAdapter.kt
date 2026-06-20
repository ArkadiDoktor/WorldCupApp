package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.data.local.entities.ScorerEntity
import com.worldcup.app.databinding.ItemScorerBinding
import com.worldcup.app.utils.getCountryFlag

class ScorersAdapter : ListAdapter<ScorerEntity, ScorersAdapter.ScorerViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScorerEntity>() {
            override fun areItemsTheSame(oldItem: ScorerEntity, newItem: ScorerEntity) =
                oldItem.playerId == newItem.playerId
            override fun areContentsTheSame(oldItem: ScorerEntity, newItem: ScorerEntity) =
                oldItem == newItem
        }
    }

    inner class ScorerViewHolder(private val binding: ItemScorerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scorer: ScorerEntity, position: Int) {
            binding.apply {
                tvRank.text = (position + 1).toString()
                tvPlayerName.text = scorer.playerName
                tvTeamName.text = scorer.teamName ?: ""
                tvGoals.text = scorer.goals.toString()
                tvAssists.text = scorer.assists?.toString() ?: "0"
                // Show team flag (team the player plays for in this tournament)
                ivTeamCrest.text = getCountryFlag(scorer.teamName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScorerViewHolder {
        val binding = ItemScorerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScorerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScorerViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}
