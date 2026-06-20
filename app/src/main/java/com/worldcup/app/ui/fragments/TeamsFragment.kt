package com.worldcup.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.worldcup.app.R
import com.worldcup.app.data.remote.models.TeamDto
import com.worldcup.app.databinding.FragmentTeamsBinding
import com.worldcup.app.ui.adapters.TeamsAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamsFragment : Fragment() {

    private var _binding: FragmentTeamsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var teamsAdapter: TeamsAdapter
    private var allTeams: List<TeamDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        observeData()
        viewModel.fetchTeams()
    }

    private fun setupRecyclerView() {
        teamsAdapter = TeamsAdapter { team ->
            showTeamDetail(team)
        }
        binding.rvTeams.apply {
            adapter = teamsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTeams(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTeams(newText)
                return true
            }
        })
    }

    private fun filterTeams(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allTeams
        } else {
            allTeams.filter { team ->
                team.name.contains(query, ignoreCase = true) ||
                        team.tla?.contains(query, ignoreCase = true) == true
            }
        }
        teamsAdapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.tvEmpty.visible()
            binding.rvTeams.gone()
        } else {
            binding.tvEmpty.gone()
            binding.rvTeams.visible()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchTeams(forceRefresh = true)
        }
    }

    private fun showTeamDetail(team: TeamDto) {
        val message = buildString {
            append(team.name)
            team.founded?.let { append("\n${getString(R.string.founded)}: $it") }
            team.venue?.let { append("\n${getString(R.string.venue)}: $it") }
            team.clubColors?.let { append("\n${getString(R.string.colors)}: $it") }
            team.coach?.name?.let { append("\n${getString(R.string.coach)}: $it") }
        }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(team.name)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun observeData() {
        viewModel.teams.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    allTeams = result.data ?: emptyList()
                    teamsAdapter.submitList(allTeams)
                    if (allTeams.isEmpty()) {
                        binding.tvEmpty.visible()
                    } else {
                        binding.tvEmpty.gone()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    showToast(result.message ?: getString(R.string.error_loading_data))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
