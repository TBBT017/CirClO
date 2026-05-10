package database;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive console application demonstrating full CRUD for every entity
 * in the CirclO Social Platform (Users, Posts, Comments, Reactions, Connections).
 *
 * Run with:
 *   mvn exec:java -Dexec.mainClass="database.SocialDemoApp"
 */
public class SocialDemoApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static int currentUserId = -1;
    private static String currentUsername = "";

    public static void main(String[] args) {
        printBanner();
        loginOrRegister();
        if (currentUserId != -1) {
            showMainMenu();
        }
        System.out.println("Goodbye!");
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    private static void loginOrRegister() {
        System.out.println("\n[1] Login   [2] Register");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (choice.equals("2")) {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (UserDAO.register(username, email, password)) {
                System.out.println("Account created!");
            } else {
                System.out.println("Registration failed (username/email already taken).");
                return;
            }
        }

        currentUserId = UserDAO.login(username, password);
        if (currentUserId != -1) {
            currentUsername = username;
            System.out.println("Logged in as " + username + " (ID: " + currentUserId + ")");
        } else {
            System.out.println("Login failed. Check credentials.");
        }
    }

    // -------------------------------------------------------------------------
    // Main menu
    // -------------------------------------------------------------------------

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n========== CirclO Main Menu ==========");
            System.out.println("Logged in as: " + currentUsername + " (ID: " + currentUserId + ")");
            System.out.println(" 1. Users");
            System.out.println(" 2. Posts");
            System.out.println(" 3. Comments");
            System.out.println(" 4. Reactions");
            System.out.println(" 5. Connections");
            System.out.println(" 0. Logout");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                usersMenu();
            } else if (c.equals("2")) {
                postsMenu();
            } else if (c.equals("3")) {
                commentsMenu();
            } else if (c.equals("4")) {
                reactionsMenu();
            } else if (c.equals("5")) {
                connectionsMenu();
            } else if (c.equals("0")) {
                currentUserId = -1;
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Users CRUD
    // -------------------------------------------------------------------------

    private static void usersMenu() {
        while (true) {
            System.out.println("\n--- Users ---");
            System.out.println(" 1. View all users");
            System.out.println(" 2. View my profile");
            System.out.println(" 3. Update my email & password");
            System.out.println(" 4. Delete my account");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                List<String> users = UserDAO.getAllUsers();
                System.out.println("\n-- All Users --");
                for (String u : users) System.out.println(u);
            } else if (c.equals("2")) {
                String profile = UserDAO.getUserById(currentUserId);
                System.out.println(profile != null ? profile : "User not found.");
            } else if (c.equals("3")) {
                System.out.print("New email: ");
                String email = scanner.nextLine().trim();
                System.out.print("New password: ");
                String pass = scanner.nextLine().trim();
                System.out.println(UserDAO.updateUser(currentUserId, email, pass)
                        ? "Profile updated." : "Update failed.");
            } else if (c.equals("4")) {
                System.out.print("Are you sure? (yes/no): ");
                if ("yes".equalsIgnoreCase(scanner.nextLine().trim())) {
                    if (UserDAO.deleteUser(currentUserId)) {
                        System.out.println("Account deleted.");
                        currentUserId = -1;
                        return;
                    } else {
                        System.out.println("Delete failed.");
                    }
                }
            } else if (c.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Posts CRUD
    // -------------------------------------------------------------------------

    private static void postsMenu() {
        while (true) {
            System.out.println("\n--- Posts ---");
            System.out.println(" 1. View all posts");
            System.out.println(" 2. View my feed (connections only)");
            System.out.println(" 3. View post by ID");
            System.out.println(" 4. Create post");
            System.out.println(" 5. Update my post");
            System.out.println(" 6. Delete my post");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                System.out.println("\n-- All Posts --");
                for (String p : PostDAO.getAllPosts()) System.out.println(p);
            } else if (c.equals("2")) {
                System.out.println("\n-- Your Feed --");
                List<String> feed = PostDAO.getFeed(currentUserId);
                if (feed.isEmpty()) System.out.println("(no posts from connections)");
                else for (String p : feed) System.out.println(p);
            } else if (c.equals("3")) {
                System.out.print("Post ID: ");
                int id = readInt();
                String post = PostDAO.getPostById(id);
                System.out.println(post != null ? post : "Post not found.");
            } else if (c.equals("4")) {
                System.out.print("Content: ");
                String content = scanner.nextLine().trim();
                System.out.println(PostDAO.createPost(currentUserId, content)
                        ? "Post created." : "Create failed.");
            } else if (c.equals("5")) {
                System.out.print("Post ID to edit: ");
                int pid = readInt();
                System.out.print("New content: ");
                String nc = scanner.nextLine().trim();
                System.out.println(PostDAO.updatePost(pid, currentUserId, nc)
                        ? "Post updated." : "Update failed.");
            } else if (c.equals("6")) {
                System.out.print("Post ID to delete: ");
                int pid = readInt();
                System.out.println(PostDAO.deletePost(pid, currentUserId)
                        ? "Post deleted." : "Delete failed.");
            } else if (c.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Comments CRUD
    // -------------------------------------------------------------------------

    private static void commentsMenu() {
        while (true) {
            System.out.println("\n--- Comments ---");
            System.out.println(" 1. View comments on a post");
            System.out.println(" 2. Add a comment");
            System.out.println(" 3. Update my comment");
            System.out.println(" 4. Delete my comment");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                System.out.print("Post ID: ");
                int pid = readInt();
                List<String> comments = CommentDAO.getCommentsByPost(pid);
                if (comments.isEmpty()) System.out.println("(no comments)");
                else for (String cm : comments) System.out.println(cm);
            } else if (c.equals("2")) {
                System.out.print("Post ID: ");
                int pid = readInt();
                System.out.print("Comment: ");
                String content = scanner.nextLine().trim();
                System.out.println(CommentDAO.createComment(pid, currentUserId, content)
                        ? "Comment added." : "Add failed.");
            } else if (c.equals("3")) {
                System.out.print("Comment ID to edit: ");
                int cid = readInt();
                System.out.print("New text: ");
                String nc = scanner.nextLine().trim();
                System.out.println(CommentDAO.updateComment(cid, currentUserId, nc)
                        ? "Comment updated." : "Update failed.");
            } else if (c.equals("4")) {
                System.out.print("Comment ID to delete: ");
                int cid = readInt();
                System.out.println(CommentDAO.deleteComment(cid, currentUserId)
                        ? "Comment deleted." : "Delete failed.");
            } else if (c.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Reactions CRUD
    // -------------------------------------------------------------------------

    private static void reactionsMenu() {
        while (true) {
            System.out.println("\n--- Reactions ---");
            System.out.println(" 1. View reactions on a post");
            System.out.println(" 2. Add / change my reaction");
            System.out.println(" 3. Update reaction by ID");
            System.out.println(" 4. Delete my reaction by ID");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                System.out.print("Post ID: ");
                int pid = readInt();
                List<String> rx = ReactionDAO.getReactionsByPost(pid);
                if (rx.isEmpty()) System.out.println("(no reactions)");
                else for (String r : rx) System.out.println(r);
            } else if (c.equals("2")) {
                System.out.print("Post ID: ");
                int pid = readInt();
                System.out.print("Reaction type (like/heart/laugh): ");
                String type = scanner.nextLine().trim();
                System.out.println(ReactionDAO.addReaction(pid, currentUserId, type)
                        ? "Reaction saved." : "Reaction failed.");
            } else if (c.equals("3")) {
                System.out.print("Reaction ID: ");
                int rid = readInt();
                System.out.print("New type (like/heart/laugh): ");
                String nt = scanner.nextLine().trim();
                System.out.println(ReactionDAO.updateReaction(rid, currentUserId, nt)
                        ? "Reaction updated." : "Update failed.");
            } else if (c.equals("4")) {
                System.out.print("Reaction ID to delete: ");
                int rid = readInt();
                System.out.println(ReactionDAO.deleteReaction(rid, currentUserId)
                        ? "Reaction deleted." : "Delete failed.");
            } else if (c.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Connections CRUD
    // -------------------------------------------------------------------------

    private static void connectionsMenu() {
        while (true) {
            System.out.println("\n--- Connections ---");
            System.out.println(" 1. View my connections");
            System.out.println(" 2. Send connection request");
            System.out.println(" 3. Accept / reject a request");
            System.out.println(" 4. Remove a connection");
            System.out.println(" 0. Back");
            System.out.print("Choice: ");
            String c = scanner.nextLine().trim();
            if (c.equals("1")) {
                List<String> conns = ConnectionDAO.getConnections(currentUserId);
                if (conns.isEmpty()) System.out.println("(no connections)");
                else for (String cn : conns) System.out.println(cn);
            } else if (c.equals("2")) {
                System.out.print("Receiver user ID: ");
                int rid = readInt();
                System.out.println(ConnectionDAO.sendRequest(currentUserId, rid)
                        ? "Request sent." : "Request failed (already sent?).");
            } else if (c.equals("3")) {
                System.out.print("Connection ID: ");
                int cid = readInt();
                System.out.print("New status (accepted/rejected): ");
                String status = scanner.nextLine().trim();
                System.out.println(ConnectionDAO.updateStatus(cid, currentUserId, status)
                        ? "Status updated." : "Update failed.");
            } else if (c.equals("4")) {
                System.out.print("Connection ID to remove: ");
                int cid = readInt();
                System.out.println(ConnectionDAO.deleteConnection(cid, currentUserId)
                        ? "Connection removed." : "Remove failed.");
            } else if (c.equals("0")) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("(invalid number, using 0)");
            return 0;
        }
    }

    private static void printBanner() {
        System.out.println("============================================");
        System.out.println("   CirclO - Micro Social Platform (CMPE172)");
        System.out.println("   Team: Dushan Siriwardana & Bui Bao Tran Tran");
        System.out.println("============================================");
    }
}
