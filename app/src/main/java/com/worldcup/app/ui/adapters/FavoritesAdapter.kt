package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.R
import com.worldcup.app.data.local.entities.FavoriteTeamEntity
import com.worldcup.app.databinding.ItemFavoriteTeamBinding
import com.worldcup.app.utils.getCountryFlag

class FavoritesAdapter(
    private val onRemoveClick: (FavoriteTeamEntity) -> Unit
) : ListAdapter<FavoriteTeamEntity, FavoritesAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteTeamEntity>() {
            override fun areItemsTheSame(oldItem: FavoriteTeamEntity, newItem: FavoriteTeamEntity) =
                oldItem.teamId == newItem.teamId
            override fun areContentsTheSame(oldItem: FavoriteTeamEntity, newItem: FavoriteTeamEntity) =
                oldItem == newItem
        }
    }

    fun getItemAt(position: Int): FavoriteTeamEntity = getItem(position)

    inner class FavoriteViewHolder(private val binding: ItemFavoriteTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(team: FavoriteTeamEntity) {
            binding.apply {
                tvTeamName.text = team.name
                ivTeamCrest.text = getCountryFlag(team.name)
                tvGroup.text = team.group ?: itemView.context.getString(R.string.no_group)
                tvPoints.text = itemView.context.getString(R.string.points_format, team.points)
                tvRecord.text = itemView.context.getString(R.string.record_format, team.won, team.draw, team.lost)
                tvGoals.text = itemView.context.getString(R.string.goals_format, team.goalsFor, team.goalsAgainst)
                btnRemove.setOnClickListener { onRemoveClick(team) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
