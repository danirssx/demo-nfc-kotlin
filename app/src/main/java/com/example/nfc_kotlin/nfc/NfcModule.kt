package com.example.nfc_kotlin.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NfcModule(private val activity: Activity) {
    
    private var nfcAdapter: NfcAdapter? = null
    private var isReading = false
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    interface NfcCallback {
        fun onTagDetected(tagInfo: String)
        fun onApiResponse(success: Boolean, message: String)
    }
    
    private var callback: NfcCallback? = null
    
    fun setCallback(callback: NfcCallback) {
        this.callback = callback
    }
    
    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    }
    
    fun startReading() {
        val adapter = nfcAdapter ?: return
        
        isReading = true
        
        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        val intentFilters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        
        adapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
        Log.d("NfcModule", "Started NFC reading")
    }
    
    fun stopReading() {
        val adapter = nfcAdapter ?: return
        
        isReading = false
        adapter.disableForegroundDispatch(activity)
        Log.d("NfcModule", "Stopped NFC reading")
    }
    
    fun handleNewIntent(intent: Intent?) {
        if (!isReading || intent == null) return
        
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            val serialNumber = tag.id.joinToString("") { "%02X".format(it) }
            val tagType = tag.techList.firstOrNull() ?: "Unknown"
            val currentTime = System.currentTimeMillis()
            val actualDate = dateFormat.format(Date(currentTime))
            
            val tagInfo = "Tag Type: $tagType, Serial: $serialNumber, Time: $actualDate"
            Log.d("NfcModule", "NFC Tag detected - $tagInfo")
            
            // Notify callback immediately
            callback?.onTagDetected(tagInfo)
            
            // Send to API instantly
            sendTagToApi(serialNumber, tagType, actualDate, currentTime)
        }
    }
    
    private fun sendTagToApi(serialNumber: String, tagType: String, actualDate: String, timestamp: Long) {
        val json = JSONObject().apply {
            put("id", UUID.randomUUID().toString())
            put("tag_type", tagType)
            put("serial_number", serialNumber)
            put("actual_date", actualDate)
            put("location", "Mobile Device") // You can make this configurable
            put("timestamp", timestamp)
        }
        
        val body = json.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("http://localhost:3000/api/read")
            .post(body)
            .build()
            
        // Execute immediately for instant communication
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NfcModule", "Failed to send NFC data to API", e)
                callback?.onApiResponse(false, "API Error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("NfcModule", "Successfully sent NFC data to API: $responseBody")
                    callback?.onApiResponse(true, "Tag sent to API successfully")
                } else {
                    Log.e("NfcModule", "API request failed with code: ${response.code}")
                    callback?.onApiResponse(false, "API Error: ${response.code}")
                }
                response.close()
            }
        })
    }
}