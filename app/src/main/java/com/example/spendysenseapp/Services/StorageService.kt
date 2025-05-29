package com.example.spendysenseapp.Services

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadFile(path: String, data: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(data).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun downloadFile(path: String): ByteArray? {
        val ref = storage.reference.child(path)
        val MAX_SIZE: Long = 10 * 1024 * 1024 // 10MB
        return ref.getBytes(MAX_SIZE).await()
    }

    suspend fun updateFile(path: String, data: ByteArray): String {
        // Overwrites the file at the given path
        return uploadFile(path, data)
    }

    suspend fun deleteFile(path: String) {
        val ref = storage.reference.child(path)
        ref.delete().await()
    }

    suspend fun getDownloadUrl(path: String): String? {
        val ref = storage.reference.child(path)
        return ref.downloadUrl.await().toString()
    }
}

//usage
val storageService = StorageService()
//lifecycleScope.launch {
//    // Upload
//    val url = storageService.uploadFile("images/myfile.jpg", byteArray)
//    // Download
//    val data = storageService.downloadFile("images/myfile.jpg")
//    // Update
//    storageService.updateFile("images/myfile.jpg", newByteArray)
//    // Delete
//    storageService.deleteFile("images/myfile.jpg")
//}