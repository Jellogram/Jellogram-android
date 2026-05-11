# Testgram for Android

Android client for [Testgram](https://github.com/glebxdlolreal/testgram) — a self-hosted Telegram-compatible server.

## What must be configured

Before building a production APK, configure these values:

1. **Testgram server address**
   - In `TMessagesProj/jni/tgnet/ConnectionsManager.cpp`, replace `YOUR_SERVER_IP` with your server address.
   - Prefer a domain name. If you use a direct VPS IP locally, do not commit it to GitHub.

2. **Update endpoint**
   - In `gradle.properties`, set all beta update URLs to your HTTPS endpoint:
     ```properties
     BETA_PUBLIC_URL=https://testgram.xie.su/update.json
     BETA_PRIVATE_URL=https://testgram.xie.su/update.json
     BETA_HARDCORE_URL=https://testgram.xie.su/update.json
     ```
   - The app reads this URL through `BuildConfig.BETA_URL` and expects JSON with:
     ```json
     {
       "version": "12.4.2",
       "version_code": 6561,
       "file_url": "https://testgram.xie.su/testgram.apk",
       "changelog": "Update description"
     }
     ```
   - `version_code` here is `APP_VERSION_CODE` from `gradle.properties`, not the final APK `versionCode` shown by Android (`APP_VERSION_CODE * 10 + abiVersionCode`).

3. **Signing key**
   - Keystore path used by this project:
     ```text
     TMessagesProj/config/testgram-release.jks
     ```
   - Credentials are read from `gradle.properties`:
     ```properties
     RELEASE_KEY_ALIAS=testgram
     RELEASE_STORE_PASSWORD=...
     RELEASE_KEY_PASSWORD=...
     ```

## How I built the APK

The build was done on Ubuntu with OpenJDK 17, Android SDK command-line tools, Build Tools 35, API 35, and NDK r21e.

### 1. Install base packages

```bash
apt-get update
apt-get install -y openjdk-17-jdk unzip curl git
```

### 2. Install Android command-line tools

```bash
mkdir -p /root/android-sdk
curl -L -o /root/cmdline-tools.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip
unzip -q /root/cmdline-tools.zip -d /root/android-sdk
```

### 3. Install SDK/NDK components

```bash
yes | /root/android-sdk/cmdline-tools/bin/sdkmanager \
  --sdk_root=/root/android-sdk \
  "build-tools;30.0.3" \
  "build-tools;35.0.0" \
  "platforms;android-35" \
  "platform-tools" \
  "ndk;21.4.7075529"
```

For this project, `dx` is expected in Build Tools 35. Copy it from Build Tools 30:

```bash
cp /root/android-sdk/build-tools/30.0.3/dx \
   /root/android-sdk/build-tools/35.0.0/dx
cp /root/android-sdk/build-tools/30.0.3/lib/dx.jar \
   /root/android-sdk/build-tools/35.0.0/lib/dx.jar
```

### 4. Build release APK

Use low parallelism on small VPS machines to avoid Gradle daemon crashes:

```bash
export ANDROID_HOME=/root/android-sdk
export ANDROID_SDK_ROOT=/root/android-sdk
export ANDROID_NDK_HOME=/root/android-sdk/ndk/21.4.7075529
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs=-Xmx2048m \
  :TMessagesProj_App:assembleRelease
```

Output APK:

```text
TMessagesProj_App/build/outputs/apk/bundleAfat/release/app.apk
```

## Update server setup

The app update checker downloads `update.json`, compares `version` and `version_code` with the installed app, then downloads `file_url` as an APK.

### Files to publish

```text
/var/www/testgram.xie.su/update.json
/var/www/testgram.xie.su/testgram.apk
```

Example:

```bash
mkdir -p /var/www/testgram.xie.su
cp TMessagesProj_App/build/outputs/apk/bundleAfat/release/app.apk \
  /var/www/testgram.xie.su/testgram.apk

cat > /var/www/testgram.xie.su/update.json <<'JSON'
{
  "version": "12.4.2",
  "version_code": 6561,
  "file_url": "https://testgram.xie.su/testgram.apk",
  "changelog": "Testgram Android build"
}
JSON

chown -R www-data:www-data /var/www/testgram.xie.su
```

### nginx + SSL

Install nginx and certbot:

```bash
apt-get install -y nginx certbot python3-certbot-nginx
```

Create `/etc/nginx/sites-available/testgram.xie.su`:

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name testgram.xie.su;

    root /var/www/testgram.xie.su;
    index update.json;

    client_max_body_size 200m;

    location = /update.json {
        default_type application/json;
        add_header Cache-Control "no-cache, no-store, must-revalidate" always;
        try_files $uri =404;
    }

    location = /testgram.apk {
        default_type application/vnd.android.package-archive;
        add_header Content-Disposition "attachment; filename=\"testgram.apk\"" always;
        add_header Cache-Control "public, max-age=300" always;
        try_files $uri =404;
    }

    location / {
        try_files $uri $uri/ =404;
    }
}
```

Enable it and issue SSL:

```bash
ln -sf /etc/nginx/sites-available/testgram.xie.su /etc/nginx/sites-enabled/testgram.xie.su
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx || systemctl restart nginx

certbot --nginx -d testgram.xie.su \
  --non-interactive --agree-tos --register-unsafely-without-email --redirect
```

Verify:

```bash
curl -sS https://testgram.xie.su/update.json
curl -I https://testgram.xie.su/testgram.apk
```

## Passkey (WebAuthn) Support

The client supports passkey registration and login via the Android Credential Manager API.

For passkeys to work, your server domain must be verified by Android. Publish `assetlinks.json` at:

```text
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
