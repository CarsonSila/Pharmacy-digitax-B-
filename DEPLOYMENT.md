# PharmaCore — Deployment Guide

## Architecture Overview

```
Android App (APK)
       ↓  HTTPS
   Nginx:443  ──────────────────────────────────
       ↓                                        |
 Spring Boot:8080  (internal)          Certbot (auto-renew SSL)
       ↓
   MySQL:3306  (internal)
```

All services run as Docker containers on a single Linux server.
MySQL and Spring Boot are **not exposed** to the internet — only Nginx is.

---

## What You Need

| Requirement | Details |
|---|---|
| A Linux server | Ubuntu 22.04 LTS recommended. Min 2GB RAM, 20GB disk |
| A domain name | e.g. `pharmacore.yourname.com` — needed for HTTPS |
| Docker + Docker Compose | Installed on the server |
| Gmail App Password | For OTP emails |
| Gemini API Key | From Google AI Studio |

---

## Part 1 — Server Backend Deployment

### Step 1: Get a Server

Options (cheapest first):
- **Railway.app** — free tier, easy Docker deploy, good for demos
- **Render.com** — free tier with Docker support
- **DigitalOcean Droplet** — $6/month, most control (recommended for production)
- **AWS EC2 t3.micro** — free tier 1 year

For a real pharmacy production system → DigitalOcean or AWS.

---

### Step 2: Point Your Domain to the Server

In your domain registrar (Namecheap, GoDaddy, etc.), add an **A record**:

```
Type: A
Host: @  (or api, pharmacore, etc.)
Value: YOUR_SERVER_IP
TTL: 3600
```

Wait 5-30 minutes for DNS to propagate.

---

### Step 3: Install Docker on the Server

```bash
# Connect to your server
ssh root@YOUR_SERVER_IP

# Install Docker
curl -fsSL https://get.docker.com | sh

# Install Docker Compose plugin
apt-get install -y docker-compose-plugin

# Verify
docker --version
docker compose version
```

---

### Step 4: Upload the Project to the Server

**Option A — Git (recommended):**
```bash
# On the server
git clone https://github.com/YOUR_USERNAME/pharmacore-system.git
cd pharmacore-system
```

**Option B — SCP from your Windows machine:**
```powershell
# In your Windows terminal
scp -r "C:\Users\user\Desktop\PharmaCore System" root@YOUR_SERVER_IP:/opt/pharmacore
```

---

### Step 5: Create the .env File

```bash
cd /opt/pharmacore   # or wherever you uploaded

# Copy the template
cp .env.example .env

# Edit it
nano .env
```

Fill in every value:

```env
DB_ROOT_PASSWORD=SomeStrongRootPassword123!
DB_USERNAME=appuser
DB_PASSWORD=SomeStrongAppPassword456!
JWT_SECRET=<run this command and paste the output: openssl rand -base64 48>
MAIL_USERNAME=your-pharmacy@gmail.com
MAIL_PASSWORD=your_16_char_gmail_app_password
GEMINI_API_KEY=AIzaSy_your_actual_key
```

**How to generate JWT_SECRET:**
```bash
openssl rand -base64 48
# Example output: Hk3mP9xQzL7vN2rW8sY4jA6tB0cD1eF5gH7iJ9kM3nO==
# Paste that entire string as JWT_SECRET
```

---

### Step 6: Get a Free HTTPS Certificate (Let's Encrypt)

Update the domain in `nginx/nginx.conf` — replace both occurrences of `your-domain.com`:
```bash
nano nginx/nginx.conf
# Change: server_name your-domain.com;
# Change: ssl_certificate paths
```

Then run Certbot to get the certificate:
```bash
# Start only Nginx temporarily (no SSL yet — just HTTP for the ACME challenge)
docker compose up -d nginx certbot pharmacore-db

# Wait for Nginx to start, then request the certificate
docker compose run --rm certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email your@email.com \
  --agree-tos \
  --no-eff-email \
  -d your-domain.com
```

---

### Step 7: Start Everything

```bash
# Build and start all services
docker compose up -d --build

# Watch the logs
docker compose logs -f pharmacore-pos

# Check status
docker compose ps
```

You should see:
```
pharmacore-mysql     running (healthy)
pharmacore-pos-app   running (healthy)
pharmacore-nginx     running
pharmacore-certbot   running
```

**Test it:**
```bash
curl https://your-domain.com/actuator/health
# → {"status":"UP"}
```

---

### Step 8: Verify the Database Schema

The schema is auto-loaded from `HeThongBanThuoc_DB.sql` on first run.
Check it loaded correctly:

```bash
docker exec -it pharmacore-mysql mysql -u appuser -p he_thong_ban_thuoc
# Enter DB_PASSWORD

SHOW TABLES;
# Should list: NguoiDung, san_pham, DonHang, TonKho, etc.

exit
```

---

### Useful Commands

```bash
# View logs
docker compose logs -f pharmacore-pos     # Backend logs
docker compose logs -f pharmacore-nginx   # Nginx logs
docker compose logs -f pharmacore-mysql   # DB logs

# Restart one service
docker compose restart pharmacore-pos

# Stop everything
docker compose down

# Stop and delete all data (careful!)
docker compose down -v

# Rebuild after code changes
docker compose up -d --build pharmacore-pos

# Backup the database
docker exec pharmacore-mysql mysqldump -u root -p${DB_ROOT_PASSWORD} he_thong_ban_thuoc > backup_$(date +%Y%m%d).sql
```

---

## Part 2 — Android App Deployment

### Step 1: Set the Production URL

In `HeThongBanThuoc_FE/app/build.gradle.kts`, update the release URL:
```kotlin
release {
    buildConfigField("String", "BASE_URL", "\"https://your-domain.com/api/\"")
}
```

### Step 2: Update AndroidManifest.xml

For production the app should **not** allow cleartext traffic.
Open `HeThongBanThuoc_FE/app/src/main/AndroidManifest.xml` and **remove** this line:
```xml
android:usesCleartextTraffic="true"   ← DELETE THIS for production
```
This forces HTTPS only. Keep it only for local debug builds.

### Step 3: Create a Signing Keystore

You need a keystore to sign the release APK. Run this **once** and keep the keystore file safe:

```bash
keytool -genkey -v \
  -keystore pharmacore-release.jks \
  -alias pharmacore \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Add to `build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("pharmacore-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "pharmacore"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        ...
    }
}
```

### Step 4: Build the Release APK

In Android Studio: **Build → Generate Signed Bundle/APK → APK → Release**

Or from command line:
```bash
cd HeThongBanThuoc_FE
./gradlew assembleRelease
# APK at: app/build/outputs/apk/release/app-release.apk
```

### Step 5: Distribute the APK

**Option A — Direct install (easiest for internal use):**
- Share `app-release.apk` via Google Drive / WhatsApp
- Users enable "Install from unknown sources" in Android settings

**Option B — Google Play Store (for public release):**
- Create a Google Play Developer account ($25 one-time fee)
- Build an **AAB** instead: `./gradlew bundleRelease`
- Upload to Play Console → Internal testing → Production

---

## Part 3 — Local Development Workflow

For daily development, run only the database in Docker and Spring Boot in your IDE:

```bash
# Start only MySQL
docker compose -f docker-compose.dev.yml up -d

# Your IDE connection settings:
# URL: jdbc:mysql://localhost:3306/he_thong_ban_thuoc
# Username: devuser
# Password: devpassword
```

Run Spring Boot from IntelliJ IDEA with the environment variables set in the Run Configuration:
```
DB_URL=jdbc:mysql://localhost:3306/he_thong_ban_thuoc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=devuser
DB_PASSWORD=devpassword
JWT_SECRET=local-dev-secret-not-used-in-production-at-all
GEMINI_API_KEY=your_key
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password
UPLOAD_DIR=uploads
```

The Android emulator connects to your local Spring Boot via `http://10.0.2.2:8080/api/`.

---

## Deployment Checklist

Before going live, confirm every item:

### Backend
- [ ] `.env` created with real secrets (not `.env.example` values)
- [ ] `JWT_SECRET` is at least 48 random base64 characters
- [ ] Domain DNS A record pointing to server IP
- [ ] SSL certificate obtained via Certbot
- [ ] `docker compose ps` shows all services healthy
- [ ] `curl https://your-domain.com/actuator/health` returns `{"status":"UP"}`
- [ ] Database schema loaded (run `SHOW TABLES` to verify)
- [ ] Test login from Android or Postman

### Android
- [ ] `BASE_URL` in `build.gradle.kts` points to `https://your-domain.com/api/`
- [ ] `android:usesCleartextTraffic="true"` removed from `AndroidManifest.xml`
- [ ] Release APK signed with keystore
- [ ] Tested on a real device (not just emulator)
- [ ] Login, product browse, cart, order, chat all working

### Security
- [ ] Run `docker exec pharmacore-mysql mysql -u appuser -p` — confirm appuser can only access `he_thong_ban_thuoc`
- [ ] Confirm port 8080 is NOT accessible directly from internet: `curl http://your-server-ip:8080` should time out
- [ ] Confirm port 3307/3306 is NOT accessible from internet

---

## Troubleshooting

**Spring Boot fails to start:**
```bash
docker compose logs pharmacore-pos
# Common cause: DB_USERNAME/DB_PASSWORD wrong, or JWT_SECRET too short
```

**Database not initialized:**
```bash
# The init SQL only runs on first volume creation
# To re-run it, delete the volume:
docker compose down -v
docker compose up -d
```

**Certificate errors (ERR_SSL_PROTOCOL_ERROR):**
```bash
# Check certificate exists
ls /etc/letsencrypt/live/your-domain.com/
# Re-run certbot if missing
```

**Android can't connect in production:**
- Confirm `BASE_URL` in release build ends with `/api/`
- Confirm `usesCleartextTraffic` is removed
- Test endpoint with Postman first
