package com.worldcup.app.data.remote.firebase

// Represents a single user's prediction for a match, stored in Firestore
data class Prediction(
    val matchId: Int = 0,
    val homeTeamName: String = "",
    val awayTeamName: String = "",
    val predictedHomeScore: Int = 0,
    val predictedAwayScore: Int = 0,
    val userName: String = "",
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // No-arg constructor required for Firestore deserialization
    constructor() : this(0, "", "", 0, 0, "", "", 0L)
}
