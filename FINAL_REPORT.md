# CirclO Social Platform - Final Project Report

**Project Title**: CirclO Social Platform - A Java Web-Based Micro-Community Application  
**Team Members**:  
- Dushan Siriwardana – 014312454 (Section 3)  
- Bui Bao Tran Tran – 017005482 (Section 4)  
**Date**: May 2, 2026  

## Introduction

**Project Overview**: CirclO is a Java console-based social platform (extendable to web) for micro-communities. Users post content (text/photo sim), comment, react (like/dislike), and connect. Uses 3-tier JDBC/MySQL architecture.

**Purpose**: Demonstrates relational DB design, JDBC CRUD, normalization in a social context.

**Matches Proposal**: 100% - Users/Posts/Comments/Reactions/Connections implemented.

## Objectives

**Primary Goals**: Develop fully functional social platform with JDBC CRUD, 3-tier architecture, BCNF database.

**Key Features**:
- **CRUD**: Users (register/login), Posts (create/read/delete/feed), Reactions (add)
- **Authentication**: Username/pw login
- **Social**: Feed (connections only), reactions (like/dislike/heart), connections
- **Search**: Feed query with JOINs/ORDER BY

## System Architecture

**Three-Tier**:
- **Presentation**: Console menu (SocialDemoApp.java) - user input/output
- **Logic**: DAOs (UserDAO, PostDAO) - business logic, SQL PreparedStatements
- **Database**: MySQL circlo_db - normalized tables w/FKs/indexes

**Technology Stack**: Java 11, Maven, MySQL 9.6, MySQL JDBC 8.2

**Database Connection**: DBConnection.java loads driver, uses DriverManager w/URL params (allowPublicKeyRetrieval). Static factory for connections.

## Database Design

**Schema Diagram** (text):
```
Users (user_id PK, username UNIQUE NOT NULL, email UNIQUE NOT NULL, pw NOT NULL)
  ↓ 1:N
Posts (post_id PK, user_id FK, content NOT NULL)
  ↓ 1:N
Comments (comment_id PK, post_id FK, user_id FK, content NOT NULL)
  ↓ 1:N
Reactions (reaction_id PK, post_id FK, user_id FK UNIQUE, reaction_type NOT NULL)
  ↓ N:N
Connections (connection_id PK, requester_id FK, receiver_id FK UNIQUE, status)
```

**BCNF Relations**:
- Users BCNF (no non-trivial FD violating candidate keys)
- Posts BCNF (user_id → attributes; no partial deps)
- Comments BCNF (determinants post_id, user_id cover all)
- Reactions BCNF (unique_reaction enforces)
- Connections BCNF (unique_connection)

**Entities**:
- Users: User accounts
- Posts: User content (text/photo sim)
- Comments: Post replies
- Reactions: Post sentiments (like/dislike)
- Connections: User relationships

**Constraints**: PKs AUTO_INCREMENT, FKs ON DELETE CASCADE, UNIQUE, NOT NULL, indexes (idx_posts_user_id etc.)

## Functional Requirements

**User Roles**: Registered users (no admin) - login/post/react/connect via console.

**Features**:
- **Add**: User register, post create, reaction add (INSERT PreparedStatement)
- **View**: Feed (JOIN Posts/Connections ORDER created_at DESC), connections
- **Update**: Reaction upsert (ON DUPLICATE KEY UPDATE)
- **Delete**: Post delete by owner (DELETE w/owner check)
- **Additional**: Feed filtering, login auth

## Non-Functional Requirements

**Performance**: Indexes on FKs (idx_posts_user_id), LIMIT in feeds.
**Scalability**: Relational design scales w/indexes.
**Security**: PreparedStatements (SQL injection), plain pw (hash future).
**Accessibility**: Console simple input.

## Implementation Details

**Code Structure**: Maven standard - `src/main/java/database/` (DBConnection.java, DAOs, apps).

**Key Snippets**:
```java
// Connection (DBConnection.java)
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
}

// Post create (PostDAO.java)
boolean createPost(int userId, String content) {
    String sql = "INSERT INTO Posts (user_id, content) VALUES (?, ?)";
    try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        pstmt.setString(2, content);
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) { e.printStackTrace(); }
    return false;
}
```

**Error Handling**: try-catch SQLException → printStackTrace() in DAOs.

## Testing and Validation

**Test Cases**:
| Feature | Test | Result |
|---------|------|--------|
| Login | alice/pass123 | Works (ID=1) |
| Register | newuser/email/pw | ✓ Duplicate fail safe |
| Post Create | create w/valid ID | ✓ (FK enforced) |
| Feed | getFeed(1) | ✓ Connections JOIN |
| Reaction | like post 1 | ✓ Upsert unique |
| Delete Post | delete own post | ✓ Owner check |

**Sample Data**: 20 entries/table (Users/Post/Comments/Reactions/Connections), no NULLs, FK valid.

**Test Results**: ComprehensiveTestApp/TestApp/SocialDemoApp all pass (DB connect, CRUD, interactive).

## Challenges and Solutions

**Challenges**:
- MySQL 8+ "Public Key Retrieval" → Added URL param
- Root pw mismatch → Updated DBConnection
- Java 11 switch → if-else
- FK violations → Valid IDs/data

**Solutions**: Param tuning, pw update, compatibility code.

## Future Enhancements

- Web UI (Servlets/JSP/Spring)
- Pw hashing (BCrypt)
- File upload (photos BLOB)
- Real-time (WebSockets)
- Advanced search/pagination

## Conclusion

**Summary**: Achieved all goals - BCNF DB, JDBC CRUD, 3-tier, tests/data. Code runs, interactive demo verifies social features.

**Reflections**: JDBC direct SQL powerful/simple. Challenges strengthened DB/JDK knowledge.

## Appendices

**Schema SQL**: See `database/create_schema.sql`

**Sample Queries**:
- Feed: `SELECT p.content FROM Posts p JOIN Connections c ON ... ORDER BY created_at DESC`
- Reaction: `INSERT ... ON DUPLICATE KEY UPDATE`
- Delete: `DELETE FROM Posts WHERE post_id = ? AND user_id = ?`
- Login: `SELECT user_id FROM Users WHERE username = ? AND password = ?`

**User Manual**: See README.md
