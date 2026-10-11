package com.sysras.sqltutor;

import com.sysras.sqltutor.service.UserService;
import com.sysras.sqltutor.service.QuestionService;
import com.sysras.sqltutor.dao.DatabaseConnection;
import com.sysras.sqltutor.ui.LoginWindow;
import javax.swing.SwingUtilities;

/**
 * This is where the program starts.
 * It connects to the database and then opens the login screen.
 */
public class SQLTutor {
    public static void main(String[] args) {
        // connect to the Access files
        DatabaseConnection dbConn = new DatabaseConnection();

        // one class for students, one class for the questions
        UserService userService = new UserService(dbConn);
        QuestionService questionService = new QuestionService(dbConn, userService);

        // open the login window
        SwingUtilities.invokeLater(() -> new LoginWindow(userService, questionService));
    }
}