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
import com.worldcup.app.databinding.FragmentMatchesBinding
import com.worldcup.app.ui.adapters.MatchesAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchesFragment : Fragment() {

    private var _binding: FragmentMatchesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var matchesAdapter: MatchesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupRecyclerView() {
        matchesAdapter = MatchesAdapter { match ->
            PredictionDialogFragment.newInstance(match)
                .show(childFragmentManager, "prediction_dialog")
        }
        binding.rvMatches.apply {
            adapter = matchesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when (tab?.position) {
                    0 -> "ALL"
                    1 -> "LIVE"
                    2 -> "UPCOMING"
                    3 -> "FINISHED"
                    else -> "ALL"
                }
                viewModel.setMatchFilter(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchMatches(forceRefresh = true)
        }
    }

    private fun observeData() {
        viewModel.filteredMatches.observe(viewLifecycleOwner) { matches ->
            matchesAdapter.submitList(matches)
            if (matches.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvMatches.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvMatches.visible()
            }
        }

        viewModel.fetchMatchesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visible()
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Success -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
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
