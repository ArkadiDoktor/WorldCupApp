package com.worldcup.app.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.worldcup.app.R
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.databinding.DialogPredictionBinding
import com.worldcup.app.ui.adapters.PredictionsAdapter
import com.worldcup.app.ui.viewmodels.WorldCupViewModel
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.gone
import com.worldcup.app.utils.visible

class PredictionDialogFragment : DialogFragment() {

    private var _binding: DialogPredictionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorldCupViewModel by activityViewModels()
    private lateinit var predictionsAdapter: PredictionsAdapter

    companion object {
        private const val ARG_MATCH_ID = "match_id"
        private const val ARG_HOME_NAME = "home_name"
        private const val ARG_AWAY_NAME = "away_name"

        fun newInstance(match: MatchEntity): PredictionDialogFragment {
            val fragment = PredictionDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_MATCH_ID, match.id)
                putString(ARG_HOME_NAME, match.homeTeamName ?: "Home")
                putString(ARG_AWAY_NAME, match.awayTeamName ?: "Away")
                putBoolean("is_finished", match.status == "FINISHED")
                putBoolean("is_live", match.status == "IN_PLAY" || match.status == "PAUSED")
            }
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPredictionBinding.inflate(layoutInflater)

        val matchId = arguments?.getInt(ARG_MATCH_ID) ?: 0
        val homeName = arguments?.getString(ARG_HOME_NAME) ?: "Home"
        val awayName = arguments?.getString(ARG_AWAY_NAME) ?: "Away"

        binding.tvDialogTitle.text = getString(R.string.predict_dialog_title)
        binding.tvHomeName.text = homeName
        binding.tvAwayName.text = awayName
        val isFinished = arguments?.getBoolean("is_finished") ?: false
        val isLocked = isFinished || (arguments?.getBoolean("is_live") ?: false)
        if (isLocked) {
            binding.etHomeScore.isEnabled = false
            binding.etAwayScore.isEnabled = false
            binding.etUsername.isEnabled = false
        }

        setupPredictionsList()
        observeData(matchId)
        viewModel.loadPredictionsForMatch(matchId)

        // Pre-fill with the user's existing prediction, if any
        viewModel.getMyPrediction(matchId) { existing ->
            existing?.let {
                binding.etHomeScore.setText(it.predictedHomeScore.toString())
                binding.etAwayScore.setText(it.predictedAwayScore.toString())
                binding.etUsername.setText(it.userName)
            }
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNegativeButton(getString(R.string.cancel), null)

        if (!isLocked) {
            builder.setPositiveButton(getString(R.string.submit_prediction)) { _, _ ->
                submitPrediction(matchId, homeName, awayName)
            }
        }

        return builder.create()
    }

    private fun setupPredictionsList() {
        predictionsAdapter = PredictionsAdapter()
        binding.rvPredictions.apply {
            adapter = predictionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun submitPrediction(matchId: Int, homeName: String, awayName: String) {
        val homeScore = binding.etHomeScore.text.toString().toIntOrNull() ?: 0
        val awayScore = binding.etAwayScore.text.toString().toIntOrNull() ?: 0
        val userName = binding.etUsername.text.toString().trim()

        viewModel.submitPrediction(matchId, homeName, awayName, homeScore, awayScore, userName)
    }

    private fun observeData(matchId: Int) {
        viewModel.currentMatchPredictions.observe(this) { result ->
            when (result) {
                is Resource.Loading -> binding.progressPredictions.visible()
                is Resource.Success -> {
                    binding.progressPredictions.gone()
                    val predictions = result.data ?: emptyList()
                    predictionsAdapter.submitList(predictions)
                    if (predictions.isEmpty()) {
                        binding.tvNoPredictions.visible()
                        binding.rvPredictions.gone()
                    } else {
                        binding.tvNoPredictions.gone()
                        binding.rvPredictions.visible()
                    }
                }
                is Resource.Error -> {
                    binding.progressPredictions.gone()
                    binding.tvNoPredictions.visible()
                    binding.rvPredictions.gone()
                }
            }
        }

        viewModel.predictionSubmitResult.observe(this) { result ->
            if (result is Resource.Success) {
                viewModel.loadPredictionsForMatch(matchId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
