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

class NfcModule(private val activity: Activity) {
    
    private var nfcAdapter: NfcAdapter? = null
    private var isReading = false
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()
    
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
            val uid = tag.id.joinToString("") { "%02X".format(it) }
            Log.d("NfcModule", "NFC Tag detected with UID: $uid")
            sendTagToApi(uid)
        }
    }
    
    private fun sendTagToApi(uid: String) {
        val json = JSONObject().apply {
            put("uid", uid)
        }
        
        val body = json.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("http://YOUR_SERVER_IP:3000/api/read")
            .post(body)
            .build()
            
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NfcModule", "Failed to send NFC data to API", e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NfcModule", "Successfully sent NFC data to API")
                } else {
                    Log.e("NfcModule", "API request failed with code: ${response.code}")
                }
                response.close()
            }
        })
    }
}