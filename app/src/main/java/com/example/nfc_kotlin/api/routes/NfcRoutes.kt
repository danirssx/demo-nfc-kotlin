package com.example.nfc_kotlin.api.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import android.util.Log
import com.example.nfc_kotlin.api.models.NfcTagData
import com.example.nfc_kotlin.api.models.ApiResponse
import com.example.nfc_kotlin.api.storage.NfcDataStorageInstance

fun Route.nfcRoutes() {
    val storage = NfcDataStorageInstance.instance
    
    route("/api") {
        
        // POST /api/read - Receive NFC tag data (existing endpoint your NFC module uses)
        post("/read") {
            try {
                val tagData = call.receive<NfcTagData>()
                Log.d("NfcAPI", "Received NFC tag: ${tagData.uid}")
                
                val savedTag = storage.saveNfcTag(tagData)
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "NFC tag processed successfully",
                        data = savedTag
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error processing NFC tag", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        message = "Error processing NFC tag: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/tags - Get all NFC tags
        get("/tags") {
            try {
                val tags = storage.getAllNfcTags()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Retrieved ${tags.size} NFC tags",
                        data = tags
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error retrieving tags", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error retrieving tags: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/tags/{id} - Get specific NFC tag by ID
        get("/tags/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing tag ID")
                )
                
                val tag = storage.getNfcTag(id)
                if (tag != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            message = "Tag found",
                            data = tag
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(success = false, message = "Tag not found")
                    )
                }
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error retrieving tag", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error retrieving tag: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/tags/serial/{serialNumber} - Get all tags with specific serial number
        get("/tags/serial/{serialNumber}") {
            try {
                val serialNumber = call.parameters["serialNumber"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing serial number")
                )
                
                val tags = storage.getTagsBySerialNumber(serialNumber)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Found ${tags.size} tags with serial: $serialNumber",
                        data = tags
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error retrieving tags by serial", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error retrieving tags: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/tags/type/{tagType} - Get all tags with specific type
        get("/tags/type/{tagType}") {
            try {
                val tagType = call.parameters["tagType"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing tag type")
                )
                
                val tags = storage.getTagsByType(tagType)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Found ${tags.size} tags with type: $tagType",
                        data = tags
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error retrieving tags by type", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error retrieving tags: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/tags/location/{location} - Get all tags from specific location
        get("/tags/location/{location}") {
            try {
                val location = call.parameters["location"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing location")
                )
                
                val tags = storage.getTagsByLocation(location)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "Found ${tags.size} tags from location: $location",
                        data = tags
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error retrieving tags by location", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error retrieving tags: ${e.message}"
                    )
                )
            }
        }
        
        // DELETE /api/tags/{id} - Delete specific NFC tag
        delete("/tags/{id}") {
            try {
                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, message = "Missing tag ID")
                )
                
                val deleted = storage.deleteNfcTag(id)
                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(success = true, message = "Tag deleted successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse(success = false, message = "Tag not found")
                    )
                }
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error deleting tag", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error deleting tag: ${e.message}"
                    )
                )
            }
        }
        
        // DELETE /api/tags - Clear all NFC tags
        delete("/tags") {
            try {
                storage.clear()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, message = "All tags cleared successfully")
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error clearing tags", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error clearing tags: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/status - Get API status and statistics
        get("/status") {
            try {
                val stats = mapOf(
                    "status" to "running",
                    "total_tags" to storage.size(),
                    "timestamp" to System.currentTimeMillis()
                )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        message = "API is running",
                        data = stats
                    )
                )
            } catch (e: Exception) {
                Log.e("NfcAPI", "Error getting status", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        message = "Error getting status: ${e.message}"
                    )
                )
            }
        }
    }
}