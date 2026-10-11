package com.sysras.sqltutor.service;

import com.sysras.sqltutor.dao.DatabaseConnection;
import com.sysras.sqltutor.dao.QuestionDAO;
import com.sysras.sqltutor.model.Question;
import com.sysras.sqltutor.model.QueryResult;
import com.sysras.sqltutor.model.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Loads the questions and runs the SQL the student types.
 * It checks the answer against the correct query and adds a point if they got it.
 *
 * @author Sahib Singh
 */
public class QuestionService {

    private DatabaseConnection dbConn;
    private QuestionDAO questionDAO;
    private UserService userService;

    /**
     * userService is passed in because a correct answer has to update the score.
     */
    public QuestionService(DatabaseConnection dbConn, UserService userService) {
        this.dbConn = dbConn;
        this.questionDAO = new QuestionDAO(dbConn);
        this.userService = userService;
    }

    // just asks the database class for every question
    public List<Question> getAllQuestions() {
        return questionDAO.getAllQuestions();
    }

    // looks up one question by its id
    public Question getQuestion(int id) {
        return questionDAO.getQuestionById(id);
    }

    // passes the search word through to the database class
    public List<Question> searchQuestions(String keyword) {
        return questionDAO.searchQuestions(keyword);
    }

    /**
     * Puts the student's school database back to the original master copy.
     */
    public boolean restoreUserDatabase(String username) {
        return dbConn.restoreUserDatabase(username);
    }

    /**
     * Runs whatever SQL the student typed on their own school database.
     * SELECT answers are marked by comparing their results to the correct query.
     * INSERT, UPDATE and DELETE get a point if at least one row changes,
     * and then the database is copied back from the master so the next
     * question still has the original data.
     */
    public QueryResult executeSQL(User user, Question question, String userSQL) {
        QueryResult result = new QueryResult();

        // don't try to run anything if the box is empty
        if (userSQL == null || userSQL.trim().isEmpty()) {
            result.setCorrect(false);
            result.setMessage("Please enter an SQL query before executing.");
            return result;
        }

        String trimmedSQL = userSQL.trim();
        boolean isSelect = trimmedSQL.toUpperCase().startsWith("SELECT");

        // run it on their copy, not the master file
        try (Connection conn = dbConn.getUserWorkingConnection(user.getUsername());
             Statement stmt = conn.createStatement()) {

            if (isSelect) {
                // run what they typed and turn it into a table for the screen
                try (ResultSet userRs = stmt.executeQuery(trimmedSQL)) {
                    DefaultTableModel userModel = buildTableModel(userRs);
                    result.setTableModel(userModel);

                    // run the correct SQL as well so we can compare the two results
                    try (Statement expStmt = conn.createStatement();
                         ResultSet expRs = expStmt.executeQuery(question.getExpectedSQL())) {
                        DefaultTableModel expModel = buildTableModel(expRs);

                        // same rows, columns and values means they got it right
                        boolean matches = compareTableModels(userModel, expModel);
                        if (matches) {
                            result.setCorrect(true);
                            result.setMessage("Correct! Your query returned the expected results.");

                            // add 1 to their score and move them on
                            awardScore(user, question.getId());
                        } else {
                            result.setCorrect(false);
                            if (userModel.getRowCount() != expModel.getRowCount()) {
                                result.setMessage("Incorrect result. Your query returned " + userModel.getRowCount() 
                                        + " rows, but expected " + expModel.getRowCount() + " rows.");
                            } else {
                                result.setMessage("Incorrect result. The columns or data returned do not match what was expected.");
                            }
                        }
                    }
                }
            } else {
                // INSERT, UPDATE and DELETE don't return a table, just a row count
                int rowsAffected = stmt.executeUpdate(trimmedSQL);
                result.setRowsAffected(rowsAffected);

                if (rowsAffected > 0) {
                    result.setCorrect(true);
                    result.setMessage("Success! " + rowsAffected + " row(s) updated in your working database.");

                    // a change actually happened, so they get the point
                    awardScore(user, question.getId());
                } else {
                    result.setCorrect(false);
                    result.setMessage("Query executed, but 0 rows were affected. Check your condition.");
                }

                // there is no result table, so show how many rows changed instead
                Vector<String> cols = new Vector<>();
                cols.add("Operation Status");
                Vector<Vector<Object>> data = new Vector<>();
                Vector<Object> row = new Vector<>();
                row.add("Rows Affected: " + rowsAffected);
                data.add(row);
                result.setTableModel(new DefaultTableModel(data, cols));

                // DELETE or UPDATE would mess up the next question, so copy the master back
                dbConn.restoreUserDatabase(user.getUsername());
                result.setMessage(result.getMessage() + " (Your practice database was automatically restored.)");
            }

        } catch (SQLException e) {
            // bad SQL (wrong table name, missing quote, etc) gets shown instead of crashing
            result.setCorrect(false);
            result.setMessage("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            result.setCorrect(false);
            result.setMessage("Error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Adds 1 point and saves the next question number so they can carry on later.
     */
    private void awardScore(User user, int questionId) {
        if (user != null && userService != null) {
            int newScore = user.getScore() + 1;
            int nextQ = questionId + 1;
            userService.updateProgress(user, nextQ, newScore);
        }
    }

    /**
     * Turns the query results into a table the JTable can show.
     * The column names go in one list, and each record goes in its own list.
     */
    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        // column headings come from the query, like FirstName or Mark
        Vector<String> columns = new Vector<>();
        for (int i = 1; i <= colCount; i++) {
            columns.add(meta.getColumnLabel(i));
        }

        // then copy every row out of the ResultSet
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columns);
    }

    /**
     * Checks the student's table against the correct one.
     * If the number of rows or columns is different, it's wrong.
     * Otherwise every cell is compared, and capital letters don't matter.
     */
    private boolean compareTableModels(DefaultTableModel userModel, DefaultTableModel expModel) {
        if (userModel.getRowCount() != expModel.getRowCount()) {
            return false;
        }
        if (userModel.getColumnCount() != expModel.getColumnCount()) {
            return false;
        }

        // check each cell. trim() drops extra spaces they might have
        for (int r = 0; r < userModel.getRowCount(); r++) {
            for (int c = 0; c < userModel.getColumnCount(); c++) {
                Object uVal = userModel.getValueAt(r, c);
                Object eVal = expModel.getValueAt(r, c);

                String uStr = (uVal == null) ? "" : uVal.toString().trim();
                String eStr = (eVal == null) ? "" : eVal.toString().trim();

                if (!uStr.equalsIgnoreCase(eStr)) {
                    return false;
                }
            }
        }
        return true;
    }
}