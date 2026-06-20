package com.worldcup.app.data.remote.api

import com.worldcup.app.data.remote.models.MatchesResponse
import com.worldcup.app.data.remote.models.ScorersResponse
import com.worldcup.app.data.remote.models.StandingsResponse
import com.worldcup.app.data.remote.models.TeamsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FootballApiService {

    // ── Endpoint 1: All World Cup matches ─────────────────────────────
    @GET("competitions/{code}/matches")
    suspend fun getMatches(
        @Path("code") competitionCode: String,
        @Query("season") season: Int
    ): Response<MatchesResponse>

    // ── Endpoint 2: Group stage standings ─────────────────────────────
    @GET("competitions/{code}/standings")
    suspend fun getStandings(
        @Path("code") competitionCode: String,
        @Query("season") season: Int
    ): Response<StandingsResponse>

    // ── Endpoint 3: Top scorers ────────────────────────────────────────
    @GET("competitions/{code}/scorers")
    suspend fun getScorers(
        @Path("code") competitionCode: String,
        @Query("season") season: Int,
        @Query("limit") limit: Int = 20
    ): Response<ScorersResponse>

    // ── Endpoint 4: All teams in the competition ───────────────────────
    @GET("competitions/{code}/teams")
    suspend fun getTeams(
        @Path("code") competitionCode: String,
        @Query("season") season: Int
    ): Response<TeamsResponse>
}
