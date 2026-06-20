package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.R
import com.worldcup.app.data.remote.models.StandingEntryDto
import com.worldcup.app.databinding.ItemStandingBinding
import com.worldcup.app.utils.getCountryFlag

class StandingsAdapter(
    private val onFavoriteClick: (StandingEntryDto, String?, Boolean) -> Unit
) : ListAdapter<StandingEntryDto, StandingsAdapter.StandingViewHolder>(DIFF_CALLBACK) {

    private var groupName: String? = null
    private val favoritedTeamIds = mutableSetOf<Int>()

    fun setGroupName(name: String?) { groupName = name }

    fun setFavoritedIds(ids: Set<Int>) {
        favoritedTeamIds.clear()
        favoritedTeamIds.addAll(ids)
        notifyDataSetChanged()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StandingEntryDto>() {
            override fun areItemsTheSame(oldItem: StandingEntryDto, newItem: StandingEntryDto) =
                oldItem.team.id == newItem.team.id
            override fun areContentsTheSame(oldItem: StandingEntryDto, newItem: StandingEntryDto) =
                oldItem == newItem
        }
    }

    inner class StandingViewHolder(private val binding: ItemStandingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: StandingEntryDto) {
            binding.apply {
                tvPosition.text = entry.position.toString()
                tvTeamName.text = entry.team.name ?: ""
                tvTeamFlag.text = getCountryFlag(entry.team.name)
                tvPoints.text = entry.points.toString()
                tvPlayed.text = entry.playedGames.toString()
                tvWon.text = entry.won.toString()
                tvDraw.text = entry.draw.toString()
                tvLost.text = entry.lost.toString()
                tvGd.text = if (entry.goalDifference >= 0) "+${entry.goalDifference}"
                            else entry.goalDifference.toString()

                val isFav = favoritedTeamIds.contains(entry.team.id)
                btnFavorite.setImageResource(
                    if (isFav) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off
                )

                btnFavorite.setOnClickListener {
                    val nowFav = favoritedTeamIds.contains(entry.team.id)
                    if (nowFav) {
                        favoritedTeamIds.remove(entry.team.id)
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
                    } else {
                        entry.team.id?.let { id -> favoritedTeamIds.add(id) }
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
                    }
                    onFavoriteClick(entry, groupName, !nowFav)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandingViewHolder {
        val binding = ItemStandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StandingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StandingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
