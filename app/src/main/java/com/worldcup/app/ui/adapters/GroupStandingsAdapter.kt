package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldcup.app.data.remote.models.StandingEntryDto
import com.worldcup.app.databinding.ItemGroupHeaderBinding
import com.worldcup.app.databinding.ItemStandingBinding
import com.worldcup.app.utils.getCountryFlag

// Sealed class to represent either a header or a team row
sealed class GroupItem {
    data class Header(val groupName: String) : GroupItem()
    data class TeamRow(val entry: StandingEntryDto, val groupName: String) : GroupItem()
}

class GroupStandingsAdapter(
    private val onFavoriteClick: (StandingEntryDto, String?, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<GroupItem> = emptyList()
    private val favoritedTeamIds = mutableSetOf<Int>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TEAM = 1
    }

    fun setData(groupsMap: Map<String, List<StandingEntryDto>>) {
        val newItems = mutableListOf<GroupItem>()
        groupsMap.keys.sorted().forEach { groupName ->
            newItems.add(GroupItem.Header(groupName))
            groupsMap[groupName]?.forEach { entry ->
                newItems.add(GroupItem.TeamRow(entry, groupName))
            }
        }
        items = newItems
        notifyDataSetChanged()
    }

    fun setFavoritedIds(ids: Set<Int>) {
        favoritedTeamIds.clear()
        favoritedTeamIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is GroupItem.Header -> VIEW_TYPE_HEADER
        is GroupItem.TeamRow -> VIEW_TYPE_TEAM
    }

    override fun getItemCount() = items.size

    // Header ViewHolder
    inner class HeaderViewHolder(private val binding: ItemGroupHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: GroupItem.Header) {
            val label = header.groupName.replace("GROUP_", "Group ")
            binding.tvGroupTitle.text = label
        }
    }

    // Team row ViewHolder
    inner class TeamViewHolder(private val binding: ItemStandingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: GroupItem.TeamRow) {
            binding.apply {
                tvPosition.text = row.entry.position.toString()
                tvTeamName.text = row.entry.team.name ?: ""
                tvTeamFlag.text = getCountryFlag(row.entry.team.name)
                tvPoints.text = row.entry.points.toString()
                tvPlayed.text = row.entry.playedGames.toString()
                tvWon.text = row.entry.won.toString()
                tvDraw.text = row.entry.draw.toString()
                tvLost.text = row.entry.lost.toString()
                tvGd.text = if (row.entry.goalDifference >= 0) "+${row.entry.goalDifference}"
                            else row.entry.goalDifference.toString()

                val isFav = favoritedTeamIds.contains(row.entry.team.id)
                btnFavorite.setImageResource(
                    if (isFav) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off
                )

                btnFavorite.setOnClickListener {
                    val nowFav = favoritedTeamIds.contains(row.entry.team.id)
                    if (nowFav) {
                        favoritedTeamIds.remove(row.entry.team.id)
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
                    } else {
                        row.entry.team.id?.let { id -> favoritedTeamIds.add(id) }
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
                    }
                    onFavoriteClick(row.entry, row.groupName, !nowFav)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(
                ItemGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            TeamViewHolder(
                ItemStandingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GroupItem.Header -> (holder as HeaderViewHolder).bind(item)
            is GroupItem.TeamRow -> (holder as TeamViewHolder).bind(item)
        }
    }
}
