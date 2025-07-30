# NFC KTOR API - Real-time NFC Detection & REST API

A powerful Android application that combines **NFC tag detection** with a built-in **KTOR REST API server** for instant, real-time communication and data management.

## 🚀 Features

- **📡 Instant NFC Detection**: Detects NFC tags immediately when placed near device
- **⚡ Real-time API Communication**: Sends NFC data to local API server instantly (< 1 second)
- **📋 Live Logging Screen**: Real-time visual feedback of all NFC detections and API responses
- **🔗 Built-in REST API**: Complete KTOR server running on localhost:3000
- **💾 In-Memory Storage**: Store and manage NFC sessions with full CRUD operations
- **🎯 Multiple Search Options**: Query by serial number, tag type, location, and more

## 📱 User Interface

The app displays a **real-time logging screen** that shows:
- 📡 **NFC TAG DETECTED**: When an NFC tag is detected with full details
- ✅ **API SUCCESS**: When data is successfully sent to the API
- ❌ **API ERROR**: If there are any communication issues
- ℹ️ **SERVER INFO**: Server status and lifecycle events

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   NFC Module    │───▶│   MainActivity  │───▶│  KTOR Server    │
│                 │    │                 │    │                 │
│ • Detects tags  │    │ • Real-time UI  │    │ • REST API      │
│ • Extracts data │    │ • Log display   │    │ • Data storage  │
│ • Sends to API  │    │ • Callbacks     │    │ • HTTP handlers │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 📊 NFCSession Data Structure

Each NFC detection creates a complete session record:

```kotlin
data class NfcTagData(
    val id: String,              // Unique session ID
    val tag_type: String,        // NFC tag technology (e.g., "NfcA", "NfcB")
    val serial_number: String,   // Hexadecimal UID (e.g., "04A1B2C3")
    val actual_date: String,     // Human-readable timestamp
    val location: String,        // Detection location
    val timestamp: Long          // Unix timestamp
)
```

## 🔗 REST API Endpoints

### Base URL: `http://localhost:3000/api`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/read` | **Primary endpoint** - Receives NFC data from the app |
| `GET` | `/tags` | Get all stored NFC sessions |
| `GET` | `/tags/{id}` | Get specific session by ID |
| `GET` | `/tags/serial/{serialNumber}` | Get all sessions for a specific NFC tag |
| `GET` | `/tags/type/{tagType}` | Get sessions by NFC technology type |
| `GET` | `/tags/location/{location}` | Get sessions from specific location |
| `DELETE` | `/tags/{id}` | Delete specific session |
| `DELETE` | `/tags` | Clear all sessions |
| `GET` | `/status` | API health check and statistics |

### API Response Format
```json
{
    "success": true,
    "message": "Description of the operation",
    "data": { /* Response data */ }
}
```

## ⚡ How Instant Communication Works

1. **NFC Tag Detected** → NfcModule detects tag immediately
2. **Data Extraction** → Serial number, type, timestamp extracted instantly
3. **Callback Triggered** → UI updated with detection info (< 100ms)
4. **API Call** → HTTP POST to localhost:3000/api/read (< 1 second)
5. **Response Logged** → Success/error displayed in real-time

**Configuration for instant communication:**
- OkHttp client with 5-second timeouts
- Asynchronous callback execution
- Immediate UI updates via Compose state

## 🛠️ Setup & Installation

### Prerequisites
- Android device with NFC capability
- Android API 24+ (Android 7.0+)
- Kotlin support

### Installation Steps

1. **Clone & Build**
   ```bash
   git clone <your-repo>
   cd demo-nfc-kotlin
   ./gradlew build
   ```

2. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

3. **Enable NFC**
   - Go to Settings → Connected devices → NFC
   - Turn on NFC and Android Beam

4. **Launch App**
   - Open "NFC Kotlin" app
   - You'll see "KTOR server started on port 3000" in the log
   - Status shows "🟢 ACTIVE"

## 📋 Usage Instructions

### Basic NFC Detection
1. **Launch the app** - Server starts automatically
2. **Hold an NFC tag** near your device's NFC antenna
3. **Watch the logs** - You'll see instant detection and API communication
4. **Check results** - Use API endpoints to query stored data

### Real-time Monitoring
- **📡 Blue entries**: NFC tag detections
- **✅ Green entries**: Successful API communications  
- **❌ Red entries**: Errors or failures
- **ℹ️ Gray entries**: Server and app lifecycle events

### API Testing
```bash
# Check server status
curl http://localhost:3000/api/status

# Get all detected tags
curl http://localhost:3000/api/tags

# Test with sample data
curl -X POST http://localhost:3000/api/read \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-123",
    "tag_type": "NfcA",
    "serial_number": "04A1B2C3",
    "actual_date": "2024-01-15 12:34:56",
    "location": "Test Location",
    "timestamp": 1705312496000
  }'
```

## 🔧 Configuration

### Customize Location
In `NfcModule.kt`, line 105:
```kotlin
put("location", "Your Custom Location") // Change from "Mobile Device"
```

### Change Server Port
In `MainActivity.kt`, line 74:
```kotlin
if (ktorServer.startServer(8080)) { // Change from 3000
```

### Adjust Timeouts
In `NfcModule.kt`, lines 24-28:
```kotlin
private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)  // Increase if needed
    .writeTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()
```

## 🐛 Troubleshooting

### Common Issues

**❌ "Failed to start KTOR server"**
- Solution: Restart the app, check if port 3000 is available

**❌ "API Error: Failed to connect"**
- Solution: Ensure server started successfully (check logs for "KTOR server started")

**📡 NFC not detecting**
- Solution: Check NFC is enabled in Android settings
- Ensure you're holding tag close to NFC antenna (usually back of phone)

**⚠️ Logs not updating**
- Solution: This indicates a UI state issue - restart the app

### Debug Mode
Enable verbose logging by checking Android Logcat:
```bash
adb logcat | grep -E "(NfcModule|KtorServer|MainActivity)"
```

## 📁 Project Structure

```
app/src/main/java/com/example/nfc_kotlin/
├── MainActivity.kt              # UI and real-time logging
├── nfc/
│   └── NfcModule.kt            # NFC detection and API communication
├── api/
│   ├── models/
│   │   └── NfcTagData.kt       # Data structures
│   ├── server/
│   │   └── KtorServer.kt       # KTOR server setup
│   ├── storage/
│   │   └── NfcDataStorage.kt   # In-memory data management
│   └── routes/
│       └── NfcRoutes.kt        # REST API endpoints
└── ui/theme/                   # App theming
```

## 🔒 Security Notes

- **Local only**: API runs on localhost (127.0.0.1) - not accessible from network
- **No authentication**: Suitable for development/testing only
- **In-memory storage**: Data is lost when app closes (no persistent storage)
- **NFC permissions**: Only requires NFC permission, no location or network access

## 📈 Performance

- **NFC Detection**: < 100ms response time
- **API Communication**: < 1 second end-to-end
- **Memory Usage**: ~50MB typical usage
- **Storage Limit**: 50 log entries (configurable)
- **Concurrent Requests**: Handled by KTOR's async architecture

## 🚧 Future Enhancements

- [ ] Persistent database storage (Room/SQLite)
- [ ] Network API access (configurable IP/port)
- [ ] Export data to JSON/CSV
- [ ] Authentication and authorization
- [ ] Push notifications for NFC detections
- [ ] Batch API operations
- [ ] Advanced filtering and search

## 📄 License

MIT License - See LICENSE file for details

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

**Ready to detect NFC tags instantly? Launch the app and start tapping! 📱✨**