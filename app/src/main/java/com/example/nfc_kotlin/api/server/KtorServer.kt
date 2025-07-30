package com.example.nfc_kotlin.api.server

import android.util.Log
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.example.nfc_kotlin.api.routes.nfcRoutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KtorServer {
    private var server: NettyApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO)
    
    fun startServer(port: Int = 3000): Boolean {
        return try {
            server = embeddedServer(Netty, port = port) {
                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                        disableHtmlEscaping()
                    }
                }
                
                install(CORS) {
                    anyHost()
                    allowMethod(HttpMethod.Options)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Delete)
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Authorization)
                }
                
                install(CallLogging)
                
                routing {
                    nfcRoutes()
                }
            }
            
            serverScope.launch {
                server?.start(wait = false)
            }
            
            Log.d("KtorServer", "Server started on port $port")
            true
        } catch (e: Exception) {
            Log.e("KtorServer", "Failed to start server", e)
            false
        }
    }
    
    fun stopServer() {
        try {
            server?.stop(1000, 2000)
            Log.d("KtorServer", "Server stopped")
        } catch (e: Exception) {
            Log.e("KtorServer", "Error stopping server", e)
        }
    }
    
    fun isRunning(): Boolean {
        return server != null
    }
}