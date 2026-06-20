package com.worldcup.app.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.app.data.local.entities.FavoriteTeamEntity
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.data.local.entities.ScorerEntity
import com.worldcup.app.data.remote.firebase.Prediction
import com.worldcup.app.data.remote.firebase.PredictionRepository
import com.worldcup.app.data.remote.models.StandingEntryDto
import com.worldcup.app.data.remote.models.StandingsResponse
import com.worldcup.app.data.remote.models.TeamDto
import com.worldcup.app.repository.WorldCupRepository
import com.worldcup.app.utils.Resource
import com.worldcup.app.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorldCupViewModel @Inject constructor(
    private val repository: WorldCupRepository,
    private val predictionRepository: PredictionRepository
) : ViewModel() {

    val allMatches: LiveData<List<MatchEntity>> = repository.getAllMatches()
    val liveMatches: LiveData<List<MatchEntity>> = repository.getLiveMatches()
    val upcomingMatches: LiveData<List<MatchEntity>> = repository.getUpcomingMatches()
    val finishedMatches: LiveData<List<MatchEntity>> = repository.getFinishedMatches()

    private val _fetchMatchesResult = MutableLiveData<Resource<List<MatchEntity>>>()
    val fetchMatchesResult: LiveData<Resource<List<MatchEntity>>> = _fetchMatchesResult

    private val _standings = MutableLiveData<Resource<StandingsResponse>>()
    val standings: LiveData<Resource<StandingsResponse>> = _standings

    val allScorers: LiveData<List<ScorerEntity>> = repository.getAllScorers()

    private val _fetchScorersResult = MutableLiveData<Resource<List<ScorerEntity>>>()
    val fetchScorersResult: LiveData<Resource<List<ScorerEntity>>> = _fetchScorersResult

    private val _teams = MutableLiveData<Resource<List<TeamDto>>>()
    val teams: LiveData<Resource<List<TeamDto>>> = _teams

    val favorites: LiveData<List<FavoriteTeamEntity>> = repository.getAllFavorites()

    private val _favoriteAction = SingleLiveEvent<String>()
    val favoriteAction: LiveData<String> = _favoriteAction

    private val _selectedMatchFilter = MutableLiveData("ALL")
    val selectedMatchFilter: LiveData<String> = _selectedMatchFilter

    val filteredMatches = MediatorLiveData<List<MatchEntity>>().apply {
        addSource(allMatches) { updateFilteredMatches() }
        addSource(finishedMatches) { updateFilteredMatches() }
        addSource(upcomingMatches) { updateFilteredMatches() }
        addSource(liveMatches) { updateFilteredMatches() }
        addSource(_selectedMatchFilter) { updateFilteredMatches() }
    }

    private fun updateFilteredMatches() {
        filteredMatches.value = when (_selectedMatchFilter.value) {
            "LIVE" -> liveMatches.value ?: emptyList()
            "UPCOMING" -> upcomingMatches.value ?: emptyList()
            "FINISHED" -> finishedMatches.value ?: emptyList()
            else -> allMatches.value ?: emptyList()
        }
    }

    fun setMatchFilter(filter: String) {
        _selectedMatchFilter.value = filter
    }

    // Home screen: live matches first, then upcoming (so a live match isn't invisible on Home)
    val homeScreenMatches = MediatorLiveData<List<MatchEntity>>().apply {
        fun update() {
            val live = liveMatches.value ?: emptyList()
            val upcoming = upcomingMatches.value ?: emptyList()
            value = (live + upcoming).distinctBy { it.id }
        }
        addSource(liveMatches) { update() }
        addSource(upcomingMatches) { update() }
    }

    // Throttling: prevents rapid tab-switching from firing repeated API calls and hitting 429.
    // Room already shows cached data instantly; network only re-fetches after the cooldown.
    private val lastFetchTimestamps = mutableMapOf<String, Long>()
    private val fetchCooldownMs = 20_000L

    private fun shouldFetch(key: String, forceRefresh: Boolean): Boolean {
        if (forceRefresh) return true
        val elapsed = System.currentTimeMillis() - (lastFetchTimestamps[key] ?: 0L)
        return elapsed >= fetchCooldownMs
    }

    private fun markFetched(key: String) {
        lastFetchTimestamps[key] = System.currentTimeMillis()
    }

    fun fetchMatches(forceRefresh: Boolean = false) {
        if (!shouldFetch("matches", forceRefresh)) return
        markFetched("matches")
        viewModelScope.launch {
            _fetchMatchesResult.value = Resource.Loading()
            repository.fetchAndCacheMatches().observeForever { result ->
                _fetchMatchesResult.value = result
            }
        }
    }

    fun fetchStandings(forceRefresh: Boolean = false) {
        if (!shouldFetch("standings", forceRefresh)) return
        markFetched("standings")
        viewModelScope.launch {
            _standings.value = Resource.Loading()
            repository.fetchStandings().observeForever { result ->
                _standings.value = result
            }
        }
    }

    fun fetchScorers(forceRefresh: Boolean = false) {
        if (!shouldFetch("scorers", forceRefresh)) return
        markFetched("scorers")
        viewModelScope.launch {
            _fetchScorersResult.value = Resource.Loading()
            repository.fetchAndCacheScorers().observeForever { result ->
                _fetchScorersResult.value = result
            }
        }
    }

    fun fetchTeams(forceRefresh: Boolean = false) {
        if (!shouldFetch("teams", forceRefresh)) return
        markFetched("teams")
        viewModelScope.launch {
            _teams.value = Resource.Loading()
            repository.fetchTeams().observeForever { result ->
                _teams.value = result
            }
        }
    }

    fun addFavorite(entry: StandingEntryDto, group: String?) {
        viewModelScope.launch {
            val entity = repository.run { entry.toFavoriteEntity(group) }
            repository.addFavorite(entity)
            _favoriteAction.postValue("Added to favorites")
        }
    }

    fun removeFavorite(teamId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(teamId)
            _favoriteAction.postValue("Removed from favorites")
        }
    }

    fun removeFavoriteEntity(team: FavoriteTeamEntity) {
        viewModelScope.launch {
            repository.removeFavorite(team.teamId)
            _favoriteAction.postValue("Removed from favorites")
        }
    }

    suspend fun isFavorite(teamId: Int): Boolean = repository.isFavorite(teamId)

    // ─── Predictions (Firebase Firestore - bonus feature) ──────────────

    private val _predictionSubmitResult = SingleLiveEvent<Resource<Unit>>()
    val predictionSubmitResult: LiveData<Resource<Unit>> = _predictionSubmitResult

    private val _currentMatchPredictions = MutableLiveData<Resource<List<Prediction>>>()
    val currentMatchPredictions: LiveData<Resource<List<Prediction>>> = _currentMatchPredictions

    fun submitPrediction(
        matchId: Int,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        userName: String
    ) {
        val prediction = Prediction(
            matchId = matchId,
            homeTeamName = homeTeam,
            awayTeamName = awayTeam,
            predictedHomeScore = homeScore,
            predictedAwayScore = awayScore,
            userName = userName.ifBlank { "Anonymous Fan" },
            deviceId = predictionRepository.getDeviceId()
        )
        viewModelScope.launch {
            predictionRepository.submitPrediction(prediction).observeForever { result ->
                _predictionSubmitResult.value = result
            }
        }
    }

    fun loadPredictionsForMatch(matchId: Int) {
        predictionRepository.getPredictionsForMatch(matchId).observeForever { result ->
            _currentMatchPredictions.value = result
        }
    }

    fun getMyPrediction(matchId: Int, onResult: (Prediction?) -> Unit) {
        predictionRepository.getMyPrediction(matchId, onResult)
    }
}
