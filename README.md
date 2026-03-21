# Testgram for Android

Android client for [Testgram](https://github.com/glebxdlolreal/testgram) — a self-hosted Telegram-compatible server.

## Building

You will need Android Studio, Android NDK, and Android SDK.

1. Clone the repo:
   ```bash
   git clone https://github.com/glebxdlolreal/testgram-android.git
   ```

2. Create a keystore and place it in `TMessagesProj/config/testgram-release.jks`

3. Set credentials in `gradle.properties`:
   ```
   RELEASE_KEY_ALIAS=your_alias
   RELEASE_STORE_PASSWORD=your_password
   RELEASE_KEY_PASSWORD=your_password
   ```

4. Build release APK:
   ```bash
   ./gradlew :TMessagesProj_App:assembleRelease
   ```

## Passkey (WebAuthn) Support

The client supports passkey registration and login via the Android Credential Manager API.

For passkeys to work, your server domain must be verified by Android. Publish `assetlinks.json` at:
```
https://your.domain.com/.well-known/assetlinks.json
```

Content:
```json
[{
  "relation": ["delegate_permission/common.get-login-creds"],
  "target": {
    "namespace": "android_app",
    "package_name": "pro.testgram.messenger",
    "sha256_cert_fingerprints": ["YOUR_APK_SHA256_FINGERPRINT"]
  }
}]
```

Get your APK fingerprint:
```bash
keytool -list -v -keystore TMessagesProj/config/testgram-release.jks -alias your_alias | grep SHA256
```

## Server

See [testgram](https://github.com/glebxdlolreal/testgram) for server setup.
