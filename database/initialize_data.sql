-- CirclO Social Platform - Sample Data Initialization
-- ≥15 entries per table, no NULLs, referential integrity

USE circlo_db;

-- 20 Users
INSERT INTO Users (username, email, password) VALUES
('alice', 'alice@circlo.com', 'pass123'), ('bob', 'bob@circlo.com', 'pass123'),
('charlie', 'charlie@circlo.com', 'pass123'), ('diana', 'diana@circlo.com', 'pass123'),
('eve', 'eve@circlo.com', 'pass123'), ('frank', 'frank@circlo.com', 'pass123'),
('grace', 'grace@circlo.com', 'pass123'), ('henry', 'henry@circlo.com', 'pass123'),
('ivy', 'ivy@circlo.com', 'pass123'), ('jack', 'jack@circlo.com', 'pass123'),
('kate', 'kate@circlo.com', 'pass123'), ('leo', 'leo@circlo.com', 'pass123'),
('mia', 'mia@circlo.com', 'pass123'), ('noah', 'noah@circlo.com', 'pass123'),
('olivia', 'olivia@circlo.com', 'pass123'), ('paul', 'paul@circlo.com', 'pass123'),
('quinn', 'quinn@circlo.com', 'pass123'), ('rachel', 'rachel@circlo.com', 'pass123'),
('sam', 'sam@circlo.com', 'pass123'), ('tina', 'tina@circlo.com', 'pass123');

-- 20 Posts (user_id 1-20)
INSERT INTO Posts (user_id, content) VALUES
(1, 'Welcome to CirclO! #social'), (2, 'Great day!'), (3, 'Learning Java JDBC'),
(4, 'MySQL tips'), (5, 'Hello friends'), (6, 'Weekend plans?'), (7, 'Coffee time'),
(8, 'New project'), (9, 'Team work'), (10, 'Coding fun'), (11, 'Database design'),
(12, 'SQL queries'), (13, 'Maven build'), (14, 'JDBC connection'), (15, 'Post feed'),
(16, 'User auth'), (17, 'Comments active'), (18, 'Reactions!'), (19, 'Connections'),
(20, 'Social platform live!');

-- 20 Comments (post_id 1-20, user_id cycle)
INSERT INTO Comments (post_id, user_id, content) VALUES
(1,2,'Nice!'), (2,3,'Yes!'), (3,4,'Cool'), (4,5,'Thanks'), (5,6,'Hi'), (6,7,'Party?'),
(7,8,'Yum'), (8,9,'Congrats'), (9,10,'Yes'), (10,1,'Fun!'), (11,2,'Good'),
(12,3,'Pro'), (13,4,'Works'), (14,5,'Connected'), (15,6,'Feed good'),
(16,7,'Secure'), (17,8,'Chatty'), (18,9,'Like'), (19,10,'Friends'),
(20,1,'Awesome!');

-- 20 Reactions (post_id 1-20, user_id 11-20 then 1-10, types like/heart/laugh)
INSERT INTO Reactions (post_id, user_id, reaction_type) VALUES
(1,11,'like'), (2,12,'heart'), (3,13,'laugh'), (4,14,'like'), (5,15,'heart'),
(6,16,'like'), (7,17,'heart'), (8,18,'laugh'), (9,19,'like'), (10,20,'heart'),
(11,1,'like'), (12,2,'heart'), (13,3,'laugh'), (14,4,'like'), (15,5,'heart'),
(16,6,'like'), (17,7,'heart'), (18,8,'laugh'), (19,9,'like'), (20,10,'heart');

-- 20 Connections (requester 1-10 to receiver 11-20, mix pending/accepted)
INSERT INTO Connections (requester_id, receiver_id, status) VALUES
(1,11,'accepted'), (2,12,'accepted'), (3,13,'pending'), (4,14,'accepted'),
(5,15,'accepted'), (6,16,'pending'), (7,17,'accepted'), (8,18,'accepted'),
(9,19,'pending'), (10,20,'accepted'), (11,1,'accepted'), (12,2,'accepted'),
(13,3,'pending'), (14,4,'accepted'), (15,5,'accepted'), (16,6,'pending'),
(17,7,'accepted'), (18,8,'accepted'), (19,9,'pending'), (20,10,'accepted');

