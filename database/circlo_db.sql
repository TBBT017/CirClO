-- CirclO Social Platform Database Schema
-- MySQL

-- Create database
CREATE DATABASE IF NOT EXISTS circlo_db;
USE circlo_db;

-- Users table
CREATE TABLE IF NOT EXISTS Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- In production, store hashed passwords
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts table
CREATE TABLE IF NOT EXISTS Posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Comments table
CREATE TABLE IF NOT EXISTS Comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Reactions table (like/heart/etc)
CREATE TABLE IF NOT EXISTS Reactions (
    reaction_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    reaction_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_reaction (post_id, user_id)
);

-- Connections table (user relationships)
CREATE TABLE IF NOT EXISTS Connections (
    connection_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    receiver_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',  -- pending, accepted, rejected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_connection (requester_id, receiver_id)
);

-- Add indexes for better performance
CREATE INDEX idx_posts_user_id ON Posts(user_id);
CREATE INDEX idx_comments_post_id ON Comments(post_id);
CREATE INDEX idx_reactions_post_id ON Reactions(post_id);
CREATE INDEX idx_connections_requester ON Connections(requester_id);
CREATE INDEX idx_connections_receiver ON Connections(receiver_id);

-- Sample data (for testing)
INSERT INTO Users (username, email, password) VALUES 
('tran', 'alice@example.com', '1234'),
('bob', 'bob@example.com', 'password123'),
('charlie', 'charlie@example.com', 'password123');

INSERT INTO Posts (user_id, content) VALUES 
(1, 'Hello CirclO! This is my first post.'),
(2, 'Excited to be here!');

INSERT INTO Comments (post_id, user_id, content) VALUES 
(1, 2, 'Welcome alice!');

INSERT INTO Connections (requester_id, receiver_id, status) VALUES 
(1, 2, 'accepted'),
(2, 3, 'pending');
