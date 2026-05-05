# CirclO Social Platform

A Java + JDBC + MySQL micro-community application.  
**Team:** Dushan Siriwardana (014312454) & Bui Bao Tran Tran (017005482)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.6+ |
| MySQL | 8.x |

---

## Setup (one-time)

### 1. Start MySQL
```bash
brew services start mysql
# or: mysql.server start
```

### 2. Create the database and tables
```bash
mysql -u root -p123456789 < database/create_schema.sql
```

### 3. Load sample data (20 rows per table)
```bash
mysql -u root -p123456789 < database/initialize_data.sql
```

> **Password:** The app connects as `root` with password `123456789`.  
> To change it, edit `src/main/java/database/DBConnection.java` line 12.

---

## Running the App

### Option A — VS Code (easiest)
1. Open the `CircloDemo.java` folder in VS Code
2. Install the **Extension Pack for Java** if prompted
3. Press `F5` → choose **"CirclO - Interactive App (SocialDemoApp)"**
4. Interact with the app in the VS Code integrated terminal

### Option B — Terminal (Maven)
```bash
# Build
mvn clean compile

# Run the interactive social app
mvn exec:java -Dexec.mainClass="database.SocialDemoApp"

# Run the automated test suite
mvn exec:java -Dexec.mainClass="database.ComprehensiveTestApp"
```

### Option C — Run the fat jar
```bash
mvn package
java -jar target/circlo-social-platform-1.0-SNAPSHOT.jar
```

---

## Demo Login Credentials

| Username | Password |
|----------|----------|
| alice    | pass123  |
| bob      | pass123  |
| charlie  | pass123  |

---

## Project Structure

```
CircloDemo.java/
├── src/main/java/database/
│   ├── DBConnection.java           # JDBC connection
│   ├── UserDAO.java                # User login & register
│   ├── PostDAO.java                # Post CRUD & feed query
│   ├── SocialDemoApp.java          # Interactive console app
│   └── ComprehensiveTestApp.java   # Automated test suite
├── database/
│   ├── create_schema.sql           # DDL: tables + constraints
│   └── initialize_data.sql         # 20 rows per table
├── .vscode/
│   ├── launch.json                 # VS Code run configs (F5)
│   └── settings.json
└── pom.xml
```

---

## Features

- **Register / Login** — JDBC auth against Users table
- **Create Post** — INSERT into Posts
- **View Feed** — JOIN across Connections + Posts
- **Add Reaction** — INSERT/UPDATE Reactions (like/heart/dislike)
- **View Connections** — list accepted connections

---

## Database Schema

```
Users(user_id PK, username UNIQUE, email UNIQUE, password, created_at)
Posts(post_id PK, user_id FK→Users, content, created_at)
Comments(comment_id PK, post_id FK→Posts, user_id FK→Users, content, created_at)
Reactions(reaction_id PK, post_id FK→Posts, user_id FK→Users, reaction_type, created_at)
Connections(connection_id PK, requester_id FK→Users, receiver_id FK→Users, status, created_at)
```

## Dependencies

All dependencies are managed by Maven (auto-downloaded on first build):
- `com.mysql:mysql-connector-j:8.2.0`
