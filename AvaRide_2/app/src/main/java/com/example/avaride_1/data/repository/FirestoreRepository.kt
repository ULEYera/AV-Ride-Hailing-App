package com.example.avaride_1.data.repository

import android.util.Log
import com.example.avaride_1.domain.model.Trip
import com.example.avaride_1.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// [PART 2] Implement Firestore User Profile & Vehicle Preferences (US-04)
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

    suspend fun getTripHistory(phoneNumber: String): List<Trip> {
        return try {
            val snapshot = usersCollection.document(phoneNumber)
                .collection("trips")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Trip::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching trip history", e)
            emptyList()
        }
    }

    suspend fun updateUserLocation(phoneNumber: String, type: String, address: String) {
        try {
            val field = if (type == "Home") "homeLocation" else "workLocation"
            val updates = mapOf(field to address)
            usersCollection.document(phoneNumber).update(updates).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error updating location", e)
            throw e
        }
    }

    suspend fun updatePaymentMethod(phoneNumber: String, method: String) {
        try {
            usersCollection.document(phoneNumber).update("paymentMethod", method).await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error updating payment method", e)
            throw e
        }
    }
}
