package com.example.nfc_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nfc_kotlin.nfc.NfcModule
import com.example.nfc_kotlin.ui.theme.NfckotlinTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var nfcModule: NfcModule
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcModule = NfcModule(this)
        
        enableEdgeToEdge()
        setContent {
            NfckotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "NFC Ready",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        nfcModule.startReading()
    }
    
    override fun onPause() {
        super.onPause()
        nfcModule.stopReading()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        nfcModule.handleNewIntent(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NfckotlinTheme {
        Greeting("Buenas rey")
    }
}