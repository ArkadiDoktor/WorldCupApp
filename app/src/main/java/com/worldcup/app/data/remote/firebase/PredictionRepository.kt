package com.worldcup.app.data.remote.firebase

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.worldcup.app.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val predictionsCollection = firestore.collection("predictions")

    // Unique-ish device identifier so a user can only have one prediction per match
    fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
    }

    // Submit or update a prediction for a specific match.
    // Document ID = matchId_deviceId so each device can only have one prediction per match.
    fun submitPrediction(prediction: Prediction): LiveData<Resource<Unit>> {
        val result = MutableLiveData<Resource<Unit>>()
        result.value = Resource.Loading()

        val docId = "${prediction.matchId}_${prediction.deviceId}"
        predictionsCollection.document(docId)
            .set(prediction)
            .addOnSuccessListener {
                result.value = Resource.Success(Unit)
            }
            .addOnFailureListener { e ->
                result.value = Resource.Error(e.localizedMessage ?: "Failed to submit prediction")
            }

        return result
    }

    // Listen in real-time to all predictions for a specific match (shared/community view)
    fun getPredictionsForMatch(matchId: Int): LiveData<Resource<List<Prediction>>> {
        val result = MutableLiveData<Resource<List<Prediction>>>()
        result.value = Resource.Loading()

        predictionsCollection
            .whereEqualTo("matchId", matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    result.value = Resource.Error(error.localizedMessage ?: "Failed to load predictions")
                    return@addSnapshotListener
                }
                val predictions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Prediction::class.java)
                } ?: emptyList()
                result.value = Resource.Success(predictions.sortedByDescending { it.timestamp })
            }

        return result
    }

    // Get the current device's own prediction for a match (to show "you predicted X-Y")
    fun getMyPrediction(matchId: Int, onResult: (Prediction?) -> Unit) {
        val docId = "${matchId}_${getDeviceId()}"
        predictionsCollection.document(docId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObject(Prediction::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
