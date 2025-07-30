# NFC KTOR REST API Endpoints

## Base URL
`http://localhost:3000/api`

## Available Endpoints

### 1. POST /api/read
**Description:** Receive NFC tag data (used by your NFC module)
**Method:** POST
**Body:**
```json
{
    "uid": "04A1B2C3",
    "timestamp": 1643723400000
}
```
**Response:**
```json
{
    "success": true,
    "message": "NFC tag processed successfully",
    "data": {
        "id": "uuid-generated",
        "uid": "04A1B2C3",
        "timestamp": 1643723400000,
        "status": "processed"
    }
}
```

### 2. GET /api/tags
**Description:** Get all stored NFC tags
**Method:** GET
**Response:**
```json
{
    "success": true,
    "message": "Retrieved X NFC tags",
    "data": [
        {
            "id": "uuid1",
            "uid": "04A1B2C3",
            "timestamp": 1643723400000,
            "status": "processed"
        }
    ]
}
```

### 3. GET /api/tags/{id}
**Description:** Get specific NFC tag by ID
**Method:** GET
**Response:**
```json
{
    "success": true,
    "message": "Tag found",
    "data": {
        "id": "uuid1",
        "uid": "04A1B2C3",
        "timestamp": 1643723400000,
        "status": "processed"
    }
}
```

### 4. GET /api/tags/uid/{uid}
**Description:** Get all tags with specific UID
**Method:** GET
**Response:**
```json
{
    "success": true,
    "message": "Found X tags with UID: 04A1B2C3",
    "data": [
        {
            "id": "uuid1",
            "uid": "04A1B2C3",
            "timestamp": 1643723400000,
            "status": "processed"
        }
    ]
}
```

### 5. DELETE /api/tags/{id}
**Description:** Delete specific NFC tag
**Method:** DELETE
**Response:**
```json
{
    "success": true,
    "message": "Tag deleted successfully"
}
```

### 6. DELETE /api/tags
**Description:** Clear all NFC tags
**Method:** DELETE
**Response:**
```json
{
    "success": true,
    "message": "All tags cleared successfully"
}
```

### 7. GET /api/status
**Description:** Get API status and statistics
**Method:** GET
**Response:**
```json
{
    "success": true,
    "message": "API is running",
    "data": {
        "status": "running",
        "total_tags": 5,
        "timestamp": 1643723400000
    }
}
```

## Testing with curl

```bash
# Test server status
curl http://localhost:3000/api/status

# Add a test NFC tag
curl -X POST http://localhost:3000/api/read \
  -H "Content-Type: application/json" \
  -d '{"uid":"04A1B2C3","timestamp":1643723400000}'

# Get all tags
curl http://localhost:3000/api/tags

# Get tags by UID
curl http://localhost:3000/api/tags/uid/04A1B2C3
```

## How It Works

1. **Your Android app starts** → KTOR server starts on port 3000
2. **NFC tag is detected** → NfcModule sends data to `POST /api/read`
3. **API processes the tag** → Stores it in memory with unique ID
4. **You can query the data** → Use other endpoints to retrieve, filter, or manage tags

The API uses in-memory storage, so data will be lost when the app is closed. For persistent storage, you could integrate with a database like Room or SQLite.