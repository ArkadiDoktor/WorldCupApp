package com.worldcup.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldcup.app.R
import com.worldcup.app.databinding.FragmentHomeBinding
import com.worldcup.app.ui.adapters.MatchesAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var upcomingAdapter: MatchesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        viewModel.fetchMatches(forceRefresh = true)
        viewModel.fetchTeams()
    }

    private fun setupRecyclerView() {
        upcomingAdapter = MatchesAdapter()
        binding.rvUpcomingMatches.apply {
            adapter = upcomingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        viewModel.homeScreenMatches.observe(viewLifecycleOwner) { matches ->
            val limited = matches.take(5)
            upcomingAdapter.submitList(limited)
            if (limited.isEmpty()) binding.tvNoUpcoming.visible()
            else binding.tvNoUpcoming.gone()
        }

        viewModel.liveMatches.observe(viewLifecycleOwner) { liveMatches ->
            if (liveMatches.isNotEmpty()) {
                binding.tvLiveBadge.visible()
                binding.tvLiveBadge.text = getString(R.string.live_count, liveMatches.size)
            } else {
                binding.tvLiveBadge.gone()
            }
        }

        viewModel.finishedMatches.observe(viewLifecycleOwner) { finished ->
            binding.tvMatchesPlayed.text = getString(R.string.matches_played, finished.size)
        }

        // Show real team count from API
        viewModel.teams.observe(viewLifecycleOwner) { result ->
            if (result is Resource.Success) {
                val count = result.data?.size ?: 0
                if (count > 0) binding.tvTeamsCount.text = getString(R.string.teams_count_dynamic, count)
            }
        }

        viewModel.fetchMatchesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> binding.progressBar.visible()
                is Resource.Success -> binding.progressBar.gone()
                is Resource.Error -> {
                    binding.progressBar.gone()
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
