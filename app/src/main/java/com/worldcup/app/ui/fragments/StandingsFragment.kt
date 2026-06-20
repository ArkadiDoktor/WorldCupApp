package com.worldcup.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.worldcup.app.R
import com.worldcup.app.data.remote.models.StandingEntryDto
import com.worldcup.app.databinding.FragmentStandingsBinding
import com.worldcup.app.ui.adapters.GroupStandingsAdapter
import com.worldcup.app.ui.adapters.StandingsAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.WorldCupGroups
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StandingsFragment : Fragment() {

    private var _binding: FragmentStandingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()

    private lateinit var groupStandingsAdapter: GroupStandingsAdapter
    private lateinit var overallAdapter: StandingsAdapter

    // groupsMap built from hardcoded groups + API stats
    private var groupsMap: Map<String, List<StandingEntryDto>> = emptyMap()
    // flat list of all teams from API (all standings entries)
    private var allTeamsFlat: List<StandingEntryDto> = emptyList()

    private var currentTab = 0
    private var dataLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStandingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupTabs()
        setupSwipeRefresh()
        observeData()
        viewModel.fetchStandings()
    }

    private fun setupAdapters() {
        groupStandingsAdapter = GroupStandingsAdapter { entry, group, isAdding ->
            if (isAdding) viewModel.addFavorite(entry, group)
            else entry.team.id?.let { viewModel.removeFavorite(it) }
        }
        overallAdapter = StandingsAdapter { entry, group, isAdding ->
            if (isAdding) viewModel.addFavorite(entry, group)
            else entry.team.id?.let { viewModel.removeFavorite(it) }
        }
        binding.rvStandings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStandings.adapter = groupStandingsAdapter
    }

    private fun setupTabs() {
        binding.tabStage.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                if (dataLoaded) switchTab(currentTab)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun switchTab(tab: Int) {
        when (tab) {
            0 -> {
                // Groups view
                binding.rvStandings.adapter = groupStandingsAdapter
                groupStandingsAdapter.setData(groupsMap)
                updateStarStates()
            }
            1 -> {
                // Overall - sorted by points across all groups
                binding.rvStandings.adapter = overallAdapter
                val sorted = allTeamsFlat
                    .sortedWith(
                        compareByDescending<StandingEntryDto> { it.points }
                            .thenByDescending { it.goalDifference }
                            .thenByDescending { it.goalsFor }
                    )
                    .mapIndexed { index, entry -> entry.copy(position = index + 1) }
                overallAdapter.submitList(sorted)
                updateStarStates()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchStandings(forceRefresh = true) }
    }

    private fun updateStarStates() {
        val favIds = viewModel.favorites.value?.map { it.teamId }?.toSet() ?: emptySet()
        groupStandingsAdapter.setFavoritedIds(favIds)
        overallAdapter.setFavoritedIds(favIds)
    }

    private fun observeData() {
        viewModel.standings.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false

                    val standings = result.data?.standings ?: emptyList()

                    // Get TOTAL table
                    val totalTable = standings.firstOrNull { it.type == "TOTAL" }?.table
                        ?: standings.firstOrNull()?.table
                        ?: emptyList()

                    // Build id->entry map from API
                    val apiEntryById = totalTable.associateBy { it.team.id }

                    // Build groups: use hardcoded group structure,
                    // fill API stats where available, otherwise 0s
                    val tempGroups = mutableMapOf<String, MutableList<StandingEntryDto>>()

                    WorldCupGroups.groups.forEach { (groupName, teamInfoList) ->
                        val entries = mutableListOf<StandingEntryDto>()
                        teamInfoList.forEach { teamInfo ->
                            val apiEntry = teamInfo.id?.let { apiEntryById[it] }
                            if (apiEntry != null) {
                                entries.add(apiEntry)
                            } else {
                                // Team not yet in API - add with 0 stats
                                entries.add(
                                    StandingEntryDto(
                                        position = 0,
                                        team = com.worldcup.app.data.remote.models.TeamRefDto(
                                            id = teamInfo.id,
                                            name = teamInfo.name,
                                            shortName = teamInfo.name,
                                            crest = null
                                        ),
                                        playedGames = 0,
                                        won = 0,
                                        draw = 0,
                                        lost = 0,
                                        goalsFor = 0,
                                        goalsAgainst = 0,
                                        goalDifference = 0,
                                        points = 0
                                    )
                                )
                            }
                        }
                        tempGroups[groupName] = entries
                    }

                    // Sort each group by points DESC, GD DESC, GF DESC
                    groupsMap = tempGroups.mapValues { (_, entries) ->
                        entries.sortedWith(
                            compareByDescending<StandingEntryDto> { it.points }
                                .thenByDescending { it.goalDifference }
                                .thenByDescending { it.goalsFor }
                        ).mapIndexed { i, e -> e.copy(position = i + 1) }
                    }

                    // Overall = all teams sorted by points
                    allTeamsFlat = totalTable.sortedWith(
                        compareByDescending<StandingEntryDto> { it.points }
                            .thenByDescending { it.goalDifference }
                            .thenByDescending { it.goalsFor }
                    ).mapIndexed { i, e -> e.copy(position = i + 1) }

                    dataLoaded = true
                    switchTab(currentTab)
                }
                is Resource.Error -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    showToast(result.message ?: getString(R.string.error_loading_data))
                }
            }
        }

        viewModel.favorites.observe(viewLifecycleOwner) {
            if (dataLoaded) updateStarStates()
        }

        viewModel.favoriteAction.observe(viewLifecycleOwner) { message ->
            showToast(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
