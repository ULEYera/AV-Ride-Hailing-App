package com.example.avaride_1.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

data class User(
    val phoneNumber: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Trip(
    val id: String = "",
    val phoneNumber: String = "",
    val destination: String = "",
    val pickup: String = "",
    val cost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUser(phoneNumber: String): User? {
        return try {
            val doc = usersCollection.document(phoneNumber).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error getting user", e)
            null
        }
    }

    suspend fun createUser(phoneNumber: String, name: String) {
        try {
            val user = User(phoneNumber = phoneNumber, name = name)
            usersCollection.document(phoneNumber).set(user).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error creating user", e)
            throw e
        }
    }

    suspend fun saveTrip(trip: Trip) {
        try {
            // Save trip under the user's "trips" subcollection
            usersCollection.document(trip.phoneNumber)
                .collection("trips")
                .document(trip.id)
                .set(trip)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error saving trip", e)
            throw e
        }
    }
}
