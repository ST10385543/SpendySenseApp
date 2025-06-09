package com.example.spendysenseapp.Services

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//this is a firestore service that fetches the user uid
class FirestoreService<T : Any>(
    private val collectionName: String,
    private val clazz: Class<T> // didnt allow me to put class, so i put clazz...
) {
    //gets the database reference
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection(collectionName)

    //generic add function
    suspend fun add(documentId: String, data: T) {
        collection.document(documentId).set(data).await()
    }

    //generic get function
    suspend fun get(documentId: String): T? {
        val snapshot = collection.document(documentId).get().await()
        return snapshot.toObject(clazz)
    }

    //generic get all function
    suspend fun getAll(): List<T> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(clazz) }
    }

    //function that gets most recent transactions
    suspend fun getMostRecent(limit: Long = 5): List<T> {
        val snapshot = collection.orderBy("dateCreated", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(clazz) }
    }
    //generic update method
    suspend fun update(documentId: String, data: Map<String, Any>) {
        collection.document(documentId).update(data).await()
    }

    //generic delete method
    suspend fun delete(documentId: String) {
        collection.document(documentId).delete().await()
    }

    //checks if a document exists
    suspend fun exists(documentId: String): Boolean {
        val snapshot = collection.document(documentId).get().await()
        return snapshot.exists()
    }


}

//usage
//val transactionService = FirestoreService("transactions", Transaction::class.java)
//lifecycleScope.launch {
//    transactionService.add("id123", transaction)
//    val item = transactionService.get("id123")
//    val all = transactionService.getAll()
//    transactionService.update("id123", mapOf("amount" to 100.0))
//    transactionService.delete("id123")
//}