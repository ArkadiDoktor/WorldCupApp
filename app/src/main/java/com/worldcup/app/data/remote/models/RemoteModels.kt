package com.worldcup.app.data.remote.models

import com.google.gson.annotations.SerializedName

// ─── Matches Response ────────────────────────────────────────────────
data class MatchesResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("matches") val matches: List<MatchDto> = emptyList()
)

data class MatchDto(
    @SerializedName("id") val id: Int,
    @SerializedName("utcDate") val utcDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("matchday") val matchday: Int?,
    @SerializedName("stage") val stage: String,
    @SerializedName("group") val group: String?,
    @SerializedName("homeTeam") val homeTeam: TeamRefDto,
    @SerializedName("awayTeam") val awayTeam: TeamRefDto,
    @SerializedName("score") val score: ScoreDto
)

data class TeamRefDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("shortName") val shortName: String?,
    @SerializedName("crest") val crest: String?
)

data class ScoreDto(
    @SerializedName("winner") val winner: String?,
    @SerializedName("fullTime") val fullTime: ScoreDetailDto,
    @SerializedName("halfTime") val halfTime: ScoreDetailDto?
)

data class ScoreDetailDto(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)

// ─── Standings Response ──────────────────────────────────────────────
data class StandingsResponse(
    @SerializedName("competition") val competition: CompetitionDto,
    @SerializedName("standings") val standings: List<StandingTableDto>
)

data class CompetitionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("emblem") val emblem: String?
)

data class StandingTableDto(
    @SerializedName("stage") val stage: String,
    @SerializedName("type") val type: String,
    @SerializedName("group") val group: String?,
    @SerializedName("table") val table: List<StandingEntryDto>
)

data class StandingEntryDto(
    @SerializedName("position") val position: Int,
    @SerializedName("team") val team: TeamRefDto,
    @SerializedName("playedGames") val playedGames: Int,
    @SerializedName("won") val won: Int,
    @SerializedName("draw") val draw: Int,
    @SerializedName("lost") val lost: Int,
    @SerializedName("goalsFor") val goalsFor: Int,
    @SerializedName("goalsAgainst") val goalsAgainst: Int,
    @SerializedName("goalDifference") val goalDifference: Int,
    @SerializedName("points") val points: Int
)

// ─── Scorers Response ────────────────────────────────────────────────
data class ScorersResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("scorers") val scorers: List<ScorerDto>
)

data class ScorerDto(
    @SerializedName("player") val player: PlayerDto,
    @SerializedName("team") val team: TeamRefDto,
    @SerializedName("goals") val goals: Int,
    @SerializedName("assists") val assists: Int?,
    @SerializedName("penalties") val penalties: Int?
)

data class PlayerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("nationality") val nationality: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("dateOfBirth") val dateOfBirth: String?
)

// ─── Teams Response ──────────────────────────────────────────────────
data class TeamsResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("teams") val teams: List<TeamDto>
)

data class TeamDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String?,
    @SerializedName("tla") val tla: String?,
    @SerializedName("crest") val crest: String?,
    @SerializedName("venue") val venue: String?,
    @SerializedName("founded") val founded: Int?,
    @SerializedName("clubColors") val clubColors: String?,
    @SerializedName("coach") val coach: CoachDto?
)

data class CoachDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("nationality") val nationality: String?
)
