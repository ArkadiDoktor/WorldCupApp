package com.worldcup.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldcup.app.R
import com.worldcup.app.databinding.FragmentScorersBinding
import com.worldcup.app.ui.adapters.ScorersAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScorersFragment : Fragment() {

    private var _binding: FragmentScorersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var scorersAdapter: ScorersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScorersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        observeData()
        viewModel.fetchScorers()
    }

    private fun setupRecyclerView() {
        scorersAdapter = ScorersAdapter()
        binding.rvScorers.apply {
            adapter = scorersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchScorers(forceRefresh = true) }
    }

    private fun observeData() {
        viewModel.allScorers.observe(viewLifecycleOwner) { scorers ->
            // Sort: goals DESC, then assists DESC
            val sorted = scorers.sortedWith(
                compareByDescending<com.worldcup.app.data.local.entities.ScorerEntity> { it.goals }
                    .thenByDescending { it.assists ?: 0 }
            )
            scorersAdapter.submitList(sorted)
            if (sorted.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvScorers.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvScorers.visible()
            }
        }

        viewModel.fetchScorersResult.observe(viewLifecycleOwner) { result ->
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
