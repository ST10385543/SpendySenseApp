package com.example.spendysenseapp.Services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService<T : Any>(
    private val collectionName: String,
    private val clazz: Class<T>
) {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(collectionName)

    suspend fun add(documentId: String, data: T) {
        collection.document(documentId).set(data).await()
    }

    suspend fun get(documentId: String): T? {
        val snapshot = collection.document(documentId).get().await()
        return snapshot.toObject(clazz)
    }

    suspend fun getAll(): List<T> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(clazz) }
    }

    suspend fun update(documentId: String, data: Map<String, Any>) {
        collection.document(documentId).update(data).await()
    }

    suspend fun delete(documentId: String) {
        collection.document(documentId).delete().await()
    }

    //checks if a document exists
    suspend fun exists(documentId: String): Boolean {
        val snapshot = collection.document(documentId).get().await()
        return snapshot.exists()
    }
}

//val transactionService = FirestoreService("transactions", Transaction::class.java)
//lifecycleScope.launch {
//    transactionService.add("id123", transaction)
//    val item = transactionService.get("id123")
//    val all = transactionService.getAll()
//    transactionService.update("id123", mapOf("amount" to 100.0))
//    transactionService.delete("id123")
//}