package com.sysras.sqltutor.dao;

import com.sysras.sqltutor.model.Question;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the questions out of SQLTutor.accdb.
 * The questions, hints and correct SQL all sit in the Questions table.
 *
 * @author Sahib Singh
 */
public class QuestionDAO {
    private DatabaseConnection dbConn;

    public QuestionDAO(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    /**
     * Gets every question, starting from question 1.
     */
    public List<Question> getAllQuestions() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM Questions ORDER BY QuestionID ASC";

        try (Connection conn = dbConn.getTutorConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Question q = new Question(
                    rs.getInt("QuestionID"),
                    rs.getString("Topic"),
                    rs.getString("QuestionText"),
                    rs.getString("ExpectedSQL"),
                    rs.getString("Hint"),
                    rs.getString("Difficulty")
                );
                list.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching questions: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds one question using its QuestionID.
     * Returns null if that id is not in the table.
     */
    public Question getQuestionById(int id) {
        String sql = "SELECT * FROM Questions WHERE QuestionID = ?";

        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Question(
                        rs.getInt("QuestionID"),
                        rs.getString("Topic"),
                        rs.getString("QuestionText"),
                        rs.getString("ExpectedSQL"),
                        rs.getString("Hint"),
                        rs.getString("Difficulty")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching question by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Looks for the word the student typed in the topic or the question text.
     * If they left the box empty, just show all the questions.
     */
    public List<Question> searchQuestions(String keyword) {
        List<Question> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllQuestions();
        }

        String sql = "SELECT * FROM Questions WHERE LOWER(Topic) LIKE ? OR LOWER(QuestionText) LIKE ? ORDER BY QuestionID ASC";
        String pattern = "%" + keyword.trim().toLowerCase() + "%";

        try (Connection conn = dbConn.getTutorConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                        rs.getInt("QuestionID"),
                        rs.getString("Topic"),
                        rs.getString("QuestionText"),
                        rs.getString("ExpectedSQL"),
                        rs.getString("Hint"),
                        rs.getString("Difficulty")
                    );
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching questions: " + e.getMessage());
        }
        return list;
    }
}