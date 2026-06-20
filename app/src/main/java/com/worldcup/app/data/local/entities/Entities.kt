package com.worldcup.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── Match entity (cached from API) ──────────────────────────────────
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val utcDate: String,
    val status: String,
    val matchday: Int?,
    val stage: String,
    val group: String?,
    val homeTeamId: Int?,
    val homeTeamName: String?,
    val homeTeamCrest: String?,
    val awayTeamId: Int?,
    val awayTeamName: String?,
    val awayTeamCrest: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val winner: String?
)

// ─── Favorite Team entity (user-managed ROOM list) ───────────────────
@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val teamId: Int,
    val name: String,
    val shortName: String?,
    val crest: String?,
    val group: String?,
    val points: Int,
    val played: Int,
    val won: Int,
    val lost: Int,
    val draw: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Scorer entity (cached from API) ────────────────────────────────
@Entity(tableName = "scorers")
data class ScorerEntity(
    @PrimaryKey val playerId: Int,
    val playerName: String,
    val nationality: String?,
    val position: String?,
    val teamName: String?,
    val teamCrest: String?,
    val goals: Int,
    val assists: Int?,
    val penalties: Int?
)
