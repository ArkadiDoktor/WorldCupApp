package com.worldcup.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.worldcup.app.data.local.entities.FavoriteTeamEntity
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.data.local.entities.ScorerEntity

@Dao
interface MatchDao {

    @Query("SELECT * FROM matches ORDER BY utcDate ASC")
    fun getAllMatches(): LiveData<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'IN_PLAY' OR status = 'PAUSED' ORDER BY utcDate ASC")
    fun getLiveMatches(): LiveData<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'TIMED' OR status = 'SCHEDULED' ORDER BY utcDate ASC")
    fun getUpcomingMatches(): LiveData<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'FINISHED' ORDER BY utcDate DESC")
    fun getFinishedMatches(): LiveData<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()
}

@Dao
interface FavoriteTeamDao {

    @Query("SELECT * FROM favorite_teams ORDER BY points DESC")
    fun getAllFavorites(): LiveData<List<FavoriteTeamEntity>>

    @Query("SELECT * FROM favorite_teams WHERE teamId = :teamId")
    suspend fun getFavoriteById(teamId: Int): FavoriteTeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(team: FavoriteTeamEntity)

    @Delete
    suspend fun deleteFavorite(team: FavoriteTeamEntity)

    @Query("DELETE FROM favorite_teams WHERE teamId = :teamId")
    suspend fun deleteFavoriteById(teamId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_teams WHERE teamId = :teamId)")
    suspend fun isFavorite(teamId: Int): Boolean
}

@Dao
interface ScorerDao {

    @Query("SELECT * FROM scorers ORDER BY goals DESC")
    fun getAllScorers(): LiveData<List<ScorerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScorers(scorers: List<ScorerEntity>)

    @Query("DELETE FROM scorers")
    suspend fun deleteAllScorers()
}
