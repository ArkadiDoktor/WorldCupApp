package com.worldcup.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.worldcup.app.R
import com.worldcup.app.databinding.FragmentFavoritesBinding
import com.worldcup.app.ui.adapters.FavoritesAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.showToast
import com.worldcup.app.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var favoritesAdapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToDelete()
        observeData()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesAdapter { team ->
            showDeleteConfirmation(team.teamId, team.name)
        }
        binding.rvFavorites.apply {
            adapter = favoritesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val team = favoritesAdapter.getItemAt(position)
                showDeleteConfirmation(team.teamId, team.name)
                // Revert visual swipe until user confirms
                favoritesAdapter.notifyItemChanged(position)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvFavorites)
    }

    private fun showDeleteConfirmation(teamId: Int, teamName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.remove_favorite_title))
            .setMessage(getString(R.string.remove_favorite_message, teamName))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                viewModel.removeFavorite(teamId)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun observeData() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoritesAdapter.submitList(favorites)
            if (favorites.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvFavorites.gone()
                binding.tvEmptyHint.visible()
            } else {
                binding.tvEmpty.gone()
                binding.rvFavorites.visible()
                binding.tvEmptyHint.gone()
            }
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
