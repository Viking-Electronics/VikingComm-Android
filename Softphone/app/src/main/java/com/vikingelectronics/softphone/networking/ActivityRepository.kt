package com.vikingelectronics.softphone.networking

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vikingelectronics.softphone.activity.ActivityEntry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

interface ActivityRepository {
    fun getAllEntries(): Flow<ActivityEntry>
}

class ActivityRepositoryImpl @Inject constructor(
    override val db: FirebaseFirestore
): FirebaseRepository(), ActivityRepository {

    override fun getAllEntries(): Flow<ActivityEntry> = flow {
        val user = getUser("5514255221u1")
        val sipAccount = getSipAccount(user) ?: return@flow

        sipAccount.devices.forEach { ref ->
            activityCollection.whereEqualTo("sourceDevice", ref)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    doc.toObject<ActivityEntry>()?.let { emit(it) }
                }
        }

    }

    suspend fun generateEntries()  {
        val storageRefs = Firebase.storage.reference.listAll().await().items
        val devicesRef = devicesCollectionRef.get().await().documents

        storageRefs.forEachIndexed { index, item ->
            val deviceDoc = if (index % 2 == 0) devicesRef[0] else devicesRef[1]
            val deviceName = deviceDoc.get("name").toString()
            val downloadUrl = item.downloadUrl.await().toString()
            val entry = hashMapOf(
                "description" to "Generated by index: $index",
                "sourceName" to deviceName,
                "sourceDevice" to deviceDoc.reference,
                "snapshotUrl" to downloadUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )

            activityCollection.add(entry).addOnSuccessListener {
                Timber.d("Entry added: ${it.id}")
            }
        }
    }
}