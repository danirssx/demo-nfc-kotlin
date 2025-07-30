
**Enable NFC permission & feature**

- In `android/app/src/main/AndroidManifest.xml` add:

```
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

**Add HTTP & JSON deps**

- In `android/app/build.gradle` (module) under `dependencies` add:

```
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
implementation 'org.json:json:20230227'
```
Sync/Gradle sync your project.

- **Create the native module file**

    - Under `android/app/src/main/java/com/yourapp/nfc/` create `NfcModule.kt`

- **Implement `NfcModule`**

    - Extend `ReactContextBaseJavaModule` and implement `ActivityEventListener`

    - In the `init { … }` block grab `NfcAdapter.getDefaultAdapter(activity)`

    - Add two `@ReactMethod` functions:

        - `startReading()` → sets up `enableForegroundDispatch` with a `PendingIntent`

        - `stopReading()` → calls `disableForegroundDispatch`

    - Override `onNewIntent(intent: Intent)` to:

        1. Check `isReading` flag

        2. Extract `Tag` via `intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)`

        3. Format `tag.id` bytes into a hex‐string UID

        4. Call your helper `sendTagToApi(uid)`

- **Implement `sendTagToApi(uid: String)`**

    - Use OkHttp to POST JSON `{"uid":"…”}` to `http://<YOUR_SERVER_IP>:3000/api/read`

    - Handle failures/errors (log or emit to JS if you like)

- **Create & register your ReactPackage**

    - In the same folder create `NfcPackage.kt` implementing `ReactPackage`

    - In `MainApplication.kt` inside `getPackages()` add `packages.add(NfcPackage())`