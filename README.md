# SkyStore Backend — Cloud File Storage API

A production-style REST API backend for **SkyStore**, a Google Drive-inspired cloud file storage platform. Built with **Java Spring Boot** and **MySQL**, it handles secure file management, OTP-based authentication, per-user data isolation, and AI assistant integration.

---

## 🚀 Features

- **OTP Email Login** — Passwordless authentication via Gmail SMTP. A 6-digit OTP is sent to the user's email, verified on the backend, and a secure token is generated and stored in MySQL for subsequent requests.
- **Token-Based Auth** — Custom `X-Auth-Token` header mechanism (no Spring Security / JWT library). Token is stored in DB and validated on every request.
- **Per-User File Isolation** — Every file is bound to a `userId`. All queries filter by the logged-in user — no user can access another user's files, even by guessing file IDs.
- **File Management** — Upload, download, soft-delete (Bin), permanent delete, restore, starred, recent, and spam — all scoped per user.
- **File Preview Support** — Backend serves files with correct `Content-Type` headers (PDF, image, video, audio, text/code) so the browser renders them inline.
- **AI Assistant** — Integrates with **Google Gemini** and **OpenAI** APIs. Users can configure their own API key via settings.
- **Storage Tracking** — Real-time storage usage per user calculated from the database.
- **Profile & Settings** — Users can update their name, avatar, theme, and notification preferences.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Email | Spring Boot Mail (Gmail SMTP) |
| Build Tool | Maven |
| AI Integration | Google Gemini API / OpenAI API |

---

## 📁 Project Structure

```
src/main/java/com/genie/Drive_BE/
├── controller/
│   ├── AuthController.java        # OTP send, verify, logout, /me
│   ├── FileController.java        # Upload, download, list, soft-delete
│   ├── StarredController.java     # Star / unstar files
│   ├── BinController.java         # Restore, permanent delete, empty bin
│   ├── RecentController.java      # Recently opened files
│   ├── SpamController.java        # Mark / unmark spam
│   ├── SearchController.java      # Search files by name
│   ├── StorageController.java     # Storage usage info
│   ├── ProfileController.java     # View / update profile
│   ├── SettingsController.java    # Theme, notifications, AI key
│   └── AiController.java          # AI chat endpoint
│
├── services/
│   ├── AuthService.java           # OTP logic, token generation
│   ├── FileServiceStorage.java    # All file operations (user-scoped)
│   ├── UserService.java           # Profile and settings logic
│   ├── AiService.java             # Gemini / OpenAI API calls
│   └── EmailService.java          # Gmail SMTP OTP sender
│
├── entity/
│   ├── FileEntity.java            # File metadata + userId
│   └── UserEntity.java            # User profile + sessionToken
│
├── repo/
│   ├── FileRepository.java        # JPA queries scoped by userId
│   └── UserRepository.java        # findByEmail, findBySessionToken
│
├── dto/
│   └── StorageInfoDTO.java        # Storage response DTO
│
└── config/
    └── WebConfig.java             # Global CORS configuration
```

---

## ⚙️ Setup & Run

### Prerequisites
- Java 21
- MySQL 8
- Maven
- Gmail account with App Password enabled

### 1. Clone the repo
```bash
git clone https://github.com/your-username/skystore-backend.git
cd skystore-backend
```

### 2. Configure `application.properties`
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/drive_db
spring.datasource.username=root
spring.datasource.password=your_password

# File storage
file.upload-dir=uploads

# Gmail SMTP (generate App Password at myaccount.google.com → Security → App Passwords)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# AI Keys (optional — users can also set their own key via settings)
ai.gemini.api-key=your_gemini_key
ai.openai.api-key=your_openai_key
```

### 3. Run
```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

---

## 🔐 Authentication Flow

```
1. POST /api/auth/send-otp    { "email": "user@example.com" }
        → OTP sent to email

2. POST /api/auth/verify-otp  { "email": "...", "otp": "123456" }
        → Returns user object with sessionToken

3. All subsequent requests include header:
        X-Auth-Token: <sessionToken>

4. POST /api/auth/logout
        → Token cleared from database
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/send-otp` | Send OTP to email |
| POST | `/api/auth/verify-otp` | Verify OTP, returns token |
| POST | `/api/auth/logout` | Invalidate token |
| GET | `/api/auth/me` | Get current user |
| POST | `/api/files/upload` | Upload a file |
| GET | `/api/files/download/{id}` | Download / preview file |
| GET | `/api/files/list` | List files in a folder |
| DELETE | `/api/files/delete/{id}` | Soft delete (move to bin) |
| GET | `/api/starred` | List starred files |
| PATCH | `/api/starred/{id}` | Toggle star |
| GET | `/api/bin` | List trashed files |
| PATCH | `/api/bin/{id}/restore` | Restore from bin |
| DELETE | `/api/bin/{id}` | Permanently delete |
| DELETE | `/api/bin` | Empty entire bin |
| GET | `/api/recent` | Recently opened files |
| GET | `/api/spam` | Spam files |
| GET | `/api/search?q=` | Search by filename |
| GET | `/api/storage` | Storage usage info |
| GET | `/api/profile` | Get profile |
| PATCH | `/api/profile` | Update profile |
| GET | `/api/settings` | Get settings |
| PATCH | `/api/settings` | Update theme / notifications |
| PATCH | `/api/settings/ai` | Update AI provider / key |
| POST | `/api/ai/chat` | AI chat message |

---

## 🔑 Key Technical Decisions

**Why token-based auth instead of sessions?**
HTTP sessions don't work reliably in cross-origin setups (React on port 3000, Spring Boot on port 8080). The Vite dev proxy strips `Set-Cookie` headers. Using a custom `X-Auth-Token` header stored in `localStorage` solves this without needing Spring Security or JWT libraries.

**Why per-userId file scoping?**
All `FileRepository` queries include `userId` as a filter parameter. Even the single-file fetch uses `findByIdAndUserId()` — so guessing a file ID from another user returns nothing. Files are also stored on disk in separate `user_{id}/` directories.

**Why inline MIME types on download?**
The backend detects the file extension and sets the correct `Content-Type` header with `Content-Disposition: inline` for previewable files. This allows the React frontend to render PDFs, images, and videos directly in the browser using blob URLs.

---

## 👤 Author

**Your Name**
- GitHub: [@prasanjitpattanaik551](https://github.com/prasanjitpattanaik551/Drive)
- LinkedIn: [Prasanjit Pattanaik](www.linkedin.com/in/prasanjit-pattanaik-34b876344)
