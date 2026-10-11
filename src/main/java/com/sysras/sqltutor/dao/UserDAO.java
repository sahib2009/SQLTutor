package com.sysras.sqltutor.dao;

import com.sysras.sqltutor.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Talks to the Users table in SQLTutor.accdb.
 * Used for registering, logging in, and saving a student's score.
 *
 * @author Sahib Singh
 */
public class UserDAO {
    private DatabaseConnection dbConn;

    public UserDAO(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Returns true if someone already registered with this username.
     * LOWER is used so "Sahib" and "sahib" count as the same name.
     */
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT UserID FROM Users WHERE LOWER(Username) = LOWER(?)";
        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new student to the Users table.
     * Returns true if the row was inserted.
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users (Username, Password, FullName, CurrentQuestionID, Score) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName().trim());
            ps.setInt(4, user.getCurrentQuestionId());
            ps.setInt(5, user.getScore());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Looks up the username and password in the Users table.
     * Returns the student if they match, otherwise null.
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setPassword(rs.getString("Password"));
                    user.setFullName(rs.getString("FullName"));
                    user.setCurrentQuestionId(rs.getInt("CurrentQuestionID"));
                    user.setScore(rs.getInt("Score"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves which question the student is on and their score.
     * Uses the UserID so we update the right person.
     */
    public boolean updateProgress(int userId, int currentQuestionId, int score) {
        String sql = "UPDATE Users SET CurrentQuestionID = ?, Score = ? WHERE UserID = ?";
        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentQuestionId);
            ps.setInt(2, score);
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating progress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes every row in Users. I used this while testing so I could start again.
     */
    public void deleteAllUsers() {
        String sql = "DELETE FROM Users";
        try (Connection conn = dbConn.getTutorConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error clearing users: " + e.getMessage());
        }
    }
}