package com.example.nfc_kotlin.api.storage

import com.example.nfc_kotlin.api.models.NfcTagData
import com.example.nfc_kotlin.api.models.NfcTagResponse
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NfcDataStorage {
    private val storage = ConcurrentHashMap<String, NfcTagResponse>()
    
    fun saveNfcTag(tagData: NfcTagData): NfcTagResponse {
        val id = if (tagData.id.isNotEmpty()) tagData.id else UUID.randomUUID().toString()
        val response = NfcTagResponse(
            id = id,
            tagType = tagData.tagType,
            serialNumber = tagData.serialNumber,
            actualDate = tagData.actualDate,
            location = tagData.location,
            timestamp = tagData.timestamp
        )
        storage[id] = response
        return response
    }
    
    fun getNfcTag(id: String): NfcTagResponse? {
        return storage[id]
    }
    
    fun getAllNfcTags(): List<NfcTagResponse> {
        return storage.values.toList().sortedByDescending { it.timestamp }
    }
    
    fun deleteNfcTag(id: String): Boolean {
        return storage.remove(id) != null
    }
    
    fun getTagsBySerialNumber(serialNumber: String): List<NfcTagResponse> {
        return storage.values.filter { it.serialNumber == serialNumber }.sortedByDescending { it.timestamp }
    }
    
    fun getTagsByType(tagType: String): List<NfcTagResponse> {
        return storage.values.filter { it.tagType.contains(tagType, ignoreCase = true) }.sortedByDescending { it.timestamp }
    }
    
    fun getTagsByLocation(location: String): List<NfcTagResponse> {
        return storage.values.filter { it.location.contains(location, ignoreCase = true) }.sortedByDescending { it.timestamp }
    }
    
    fun clear() {
        storage.clear()
    }
    
    fun size(): Int {
        return storage.size
    }
}

object NfcDataStorageInstance {
    val instance = NfcDataStorage()
}