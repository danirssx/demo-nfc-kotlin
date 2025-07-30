package com.example.nfc_kotlin.api.models

import com.google.gson.annotations.SerializedName

data class NfcTagData(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("tag_type")
    val tagType: String,
    @SerializedName("serial_number")
    val serialNumber: String,
    @SerializedName("actual_date")
    val actualDate: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class ApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Any? = null
)

data class NfcTagResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("tag_type")
    val tagType: String,
    @SerializedName("serial_number")
    val serialNumber: String,
    @SerializedName("actual_date")
    val actualDate: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("status")
    val status: String = "processed"
)