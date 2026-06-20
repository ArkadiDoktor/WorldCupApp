package com.worldcup.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.worldcup.app.data.local.dao.FavoriteTeamDao
import com.worldcup.app.data.local.dao.MatchDao
import com.worldcup.app.data.local.dao.ScorerDao
import com.worldcup.app.data.local.entities.FavoriteTeamEntity
import com.worldcup.app.data.local.entities.MatchEntity
import com.worldcup.app.data.local.entities.ScorerEntity

@Database(
    entities = [
        MatchEntity::class,
        FavoriteTeamEntity::class,
        ScorerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WorldCupDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun scorerDao(): ScorerDao
}
