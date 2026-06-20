package com.worldcup.app.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.worldcup.app.data.local.dao.FavoriteTeamDao
import com.worldcup.app.data.local.dao.MatchDao
import com.worldcup.app.data.local.dao.ScorerDao
import com.worldcup.app.data.local.entities.FavoriteTeamEntity
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.data.local.entities.ScorerEntity
import com.worldcup.app.data.remote.api.FootballApiService
import com.worldcup.app.data.remote.models.MatchDto
import com.worldcup.app.data.remote.models.ScorerDto
import com.worldcup.app.data.remote.models.StandingEntryDto
import com.worldcup.app.data.remote.models.StandingsResponse
import com.worldcup.app.utils.Constants
import com.worldcup.app.utils.NetworkUtils
import com.worldcup.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldCupRepository @Inject constructor(
    private val apiService: FootballApiService,
    private val matchDao: MatchDao,
    private val favoriteTeamDao: FavoriteTeamDao,
    private val scorerDao: ScorerDao,
    private val networkUtils: NetworkUtils
) {

    // ─── Matches ────────────────────────────────────────────────────────

    fun getAllMatches(): LiveData<List<MatchEntity>> = matchDao.getAllMatches()

    fun getLiveMatches(): LiveData<List<MatchEntity>> = matchDao.getLiveMatches()

    fun getUpcomingMatches(): LiveData<List<MatchEntity>> = matchDao.getUpcomingMatches()

    fun getFinishedMatches(): LiveData<List<MatchEntity>> = matchDao.getFinishedMatches()

    fun fetchAndCacheMatches(): LiveData<Resource<List<MatchEntity>>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection"))
            return@liveData
        }
        try {
            val response = apiService.getMatches(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (response.isSuccessful) {
                val matchDtos = response.body()?.matches ?: emptyList()
                val entities = matchDtos.map { it.toEntity() }
                matchDao.deleteAllMatches()
                matchDao.insertMatches(entities)
                emit(Resource.Success(entities))
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }

    // ─── Standings ──────────────────────────────────────────────────────

    fun fetchStandings(): LiveData<Resource<StandingsResponse>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection"))
            return@liveData
        }
        try {
            val response = apiService.getStandings(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body))
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }

    // ─── Scorers ────────────────────────────────────────────────────────

    fun getAllScorers(): LiveData<List<ScorerEntity>> = scorerDao.getAllScorers()

    fun fetchAndCacheScorers(): LiveData<Resource<List<ScorerEntity>>> = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection"))
            return@liveData
        }
        try {
            val response = apiService.getScorers(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (response.isSuccessful) {
                val scorerDtos = response.body()?.scorers ?: emptyList()
                val entities = scorerDtos.map { it.toEntity() }
                scorerDao.deleteAllScorers()
                scorerDao.insertScorers(entities)
                emit(Resource.Success(entities))
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }

    // ─── Teams ─────────────────────────────────────────────────────────

    fun fetchTeams() = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection"))
            return@liveData
        }
        try {
            val response = apiService.getTeams(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body.teams))
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown error occurred"))
        }
    }

    // ─── Favorites ─────────────────────────────────────────────────────

    fun getAllFavorites(): LiveData<List<FavoriteTeamEntity>> = favoriteTeamDao.getAllFavorites()

    suspend fun addFavorite(team: FavoriteTeamEntity) = favoriteTeamDao.insertFavorite(team)

    suspend fun removeFavorite(teamId: Int) = favoriteTeamDao.deleteFavoriteById(teamId)

    suspend fun isFavorite(teamId: Int): Boolean = favoriteTeamDao.isFavorite(teamId)

    // ─── Mappers ────────────────────────────────────────────────────────

    private fun MatchDto.toEntity() = MatchEntity(
        id = id,
        utcDate = utcDate,
        status = status,
        matchday = matchday,
        stage = stage,
        group = group,
        homeTeamId = homeTeam.id,
        homeTeamName = homeTeam.name,
        homeTeamCrest = homeTeam.crest,
        awayTeamId = awayTeam.id,
        awayTeamName = awayTeam.name,
        awayTeamCrest = awayTeam.crest,
        homeScore = score.fullTime.home,
        awayScore = score.fullTime.away,
        winner = score.winner
    )

    private fun ScorerDto.toEntity() = ScorerEntity(
        playerId = player.id,
        playerName = player.name,
        nationality = player.nationality,
        position = player.position,
        teamName = team.name,
        teamCrest = team.crest,
        goals = goals,
        assists = assists,
        penalties = penalties
    )

    fun StandingEntryDto.toFavoriteEntity(group: String?) = FavoriteTeamEntity(
        teamId = team.id ?: 0,
        name = team.name ?: "",
        shortName = team.shortName,
        crest = team.crest,
        group = group,
        points = points,
        played = playedGames,
        won = won,
        lost = lost,
        draw = draw,
        goalsFor = goalsFor,
        goalsAgainst = goalsAgainst
    )
}
