package com.worldcup.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.worldcup.app.R
import com.worldcup.app.data.local.dao.MatchDao
import com.worldcup.app.data.local.dao.ScorerDao
import com.worldcup.app.data.remote.api.FootballApiService
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.data.local.entities.ScorerEntity
import com.worldcup.app.data.remote.models.MatchDto
import com.worldcup.app.data.remote.models.ScorerDto
import com.worldcup.app.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: FootballApiService,
    private val matchDao: MatchDao,
    private val scorerDao: ScorerDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            var hasNewResults = false

            // Sync matches
            val matchesResponse = apiService.getMatches(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (matchesResponse.isSuccessful) {
                val entities = matchesResponse.body()?.matches?.map { it.toEntity() } ?: emptyList()
                matchDao.deleteAllMatches()
                matchDao.insertMatches(entities)
                val finishedCount = entities.count { it.status == "FINISHED" }
                if (finishedCount > 0) hasNewResults = true
            }

            // Sync scorers
            val scorersResponse = apiService.getScorers(
                Constants.WORLD_CUP_CODE,
                Constants.SEASON_2026
            )
            if (scorersResponse.isSuccessful) {
                val entities = scorersResponse.body()?.scorers?.map { it.toEntity() } ?: emptyList()
                scorerDao.deleteAllScorers()
                scorerDao.insertScorers(entities)
            }

            if (hasNewResults) {
                showSyncNotification()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showSyncNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "world_cup_sync_channel"
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_football)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

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
}
