# CirclO — Micro Social Platform

**CMPE 172 — Enterprise Software Platforms**  
**Team:** Dushan Siriwardana (014312454) · Bui Bao Tran Tran (017005482)  
**Submission Date:** May 9, 2026

---

## Project Overview

CirclO is a console-based micro-community application built with **Java 17**, **JDBC**, and **MySQL 8**.  
It implements a three-tier architecture (Presentation → Logic → Database) and exposes full **CRUD** operations for five entities: Users, Posts, Comments, Reactions, and Connections.

---

## Prerequisites

| Tool  | Minimum Version |
|-------|----------------|
| Java  | 17             |
| Maven | 3.6            |
| MySQL | 8.x            |

---

## Setup (one-time)

### 1. Start MySQL
```bash
brew services start mysql
# or: mysql.server start
```

### 2. Create the database schema
```bash
mysql -u root -p123456789 < database/create_schema.sql
```

### 3. Load sample data (20 rows per table)
```bash
mysql -u root -p123456789 < database/initialize_data.sql
```

> **Default password:** `123456789`  
> To use a different password, edit `DB_PASSWORD` in `src/main/java/database/DBConnection.java` (line 22).

---

## Running the Application

### Option A — Maven (recommended)
```bash
# Build
mvn clean compile

# Run the interactive app
mvn exec:java -Dexec.mainClass="database.SocialDemoApp"

# Run the automated test suite
mvn exec:java -Dexec.mainClass="database.ComprehensiveTestApp"

# Quick connection smoke-test
mvn exec:java -Dexec.mainClass="database.TestApp"
```

### Option B — Fat JAR
```bash
mvn package
java -jar target/circlo-social-platform-1.0-SNAPSHOT.jar
```

### Option C — VS Code (F5)
1. Open the project folder in VS Code.
2. Install **Extension Pack for Java** if prompted.
3. Press `F5` → choose **"CirclO – Interactive App (SocialDemoApp)"**.

---

## Demo Login Credentials

| Username | Password |
|----------|----------|
| alice    | pass123  |
| bob      | pass123  |
| charlie  | pass123  |

All 20 sample users share the password `pass123`.

---

## Project Directory Structure

```
CirclO-Micro-Social-Platform/
├── src/
│   └── main/
│       └── java/
│           └── database/
│               ├── DBConnection.java           # JDBC connection manager
│               ├── UserDAO.java                # User CRUD (login, register, update, delete)
│               ├── PostDAO.java                # Post CRUD + feed query
│               ├── CommentDAO.java             # Comment CRUD
│               ├── ReactionDAO.java            # Reaction CRUD (upsert)
│               ├── ConnectionDAO.java          # Connection CRUD (friend requests)
│               ├── SocialDemoApp.java          # Interactive console application
│               ├── ComprehensiveTestApp.java   # Automated test suite (all CRUD)
│               └── TestApp.java                # Quick DB connectivity test
├── database/
│   ├── create_schema.sql                       # DDL: tables + indexes + constraints
│   └── initialize_data.sql                     # 20 rows per table seed data
├── pom.xml                                     # Maven build + dependencies
└── README.md
```

---

## Database Schema

```
Users(user_id PK, username UNIQUE NOT NULL, email UNIQUE NOT NULL, password NOT NULL, created_at)
Posts(post_id PK, user_id FK→Users CASCADE, content NOT NULL, created_at)
Comments(comment_id PK, post_id FK→Posts CASCADE, user_id FK→Users CASCADE, content NOT NULL, created_at)
Reactions(reaction_id PK, post_id FK→Posts CASCADE, user_id FK→Users CASCADE,
          reaction_type NOT NULL, created_at, UNIQUE(post_id, user_id))
Connections(connection_id PK, requester_id FK→Users CASCADE, receiver_id FK→Users CASCADE,
            status DEFAULT 'pending', created_at, UNIQUE(requester_id, receiver_id))
```

---

## Features

| Entity      | Create | Read | Update | Delete |
|-------------|--------|------|--------|--------|
| Users       | ✓ register | ✓ login / list / getById | ✓ email + password | ✓ cascade delete |
| Posts       | ✓ create | ✓ all / feed / getById | ✓ content | ✓ owner only |
| Comments    | ✓ add | ✓ by post | ✓ owner only | ✓ owner only |
| Reactions   | ✓ upsert | ✓ by post | ✓ type | ✓ owner only |
| Connections | ✓ send request | ✓ list both directions | ✓ accept/reject | ✓ either party |

---

## Dependencies

Managed by Maven — downloaded automatically on first build:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>
```

---

## Configuration Notes

- Database URL: `jdbc:mysql://localhost:3306/circlo_db`
- All URL parameters (`useSSL=false`, `serverTimezone=UTC`, `allowPublicKeyRetrieval=true`) are set in `DBConnection.java`.
- Change host/port by editing `DB_URL` in `DBConnection.java`.
