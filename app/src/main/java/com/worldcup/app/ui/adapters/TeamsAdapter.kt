package com.worldcup.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.worldcup.app.R
import com.worldcup.app.data.remote.models.TeamDto
import com.worldcup.app.databinding.ItemTeamBinding

class TeamsAdapter(
    private val onTeamClick: (TeamDto) -> Unit
) : ListAdapter<TeamDto, TeamsAdapter.TeamViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TeamDto>() {
            override fun areItemsTheSame(oldItem: TeamDto, newItem: TeamDto) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TeamDto, newItem: TeamDto) =
                oldItem == newItem
        }
    }

    inner class TeamViewHolder(private val binding: ItemTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(team: TeamDto) {
            binding.apply {
                tvTeamName.text = team.name
                tvTeamTla.text = team.tla ?: ""
                tvVenue.text = team.venue ?: itemView.context.getString(R.string.unknown_venue)
                tvFounded.text = team.founded?.toString()
                    ?: itemView.context.getString(R.string.unknown)
                tvCoach.text = team.coach?.name
                    ?: itemView.context.getString(R.string.unknown_coach)

                Glide.with(itemView.context)
                    .load(team.crest)
                    .placeholder(R.drawable.ic_football)
                    .error(R.drawable.ic_football)
                    .into(ivTeamCrest)

                root.setOnClickListener { onTeamClick(team) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
