package com.sysras.sqltutor.service;

import com.sysras.sqltutor.dao.DatabaseConnection;
import com.sysras.sqltutor.dao.UserDAO;
import com.sysras.sqltutor.model.User;

/**
 * Checks logins and new accounts before anything is saved.
 * Also updates the score and which question the student is on.
 *
 * @author Sahib Singh
 */
public class UserService {

    private DatabaseConnection dbConn;
    private UserDAO userDAO;

    /**
     * Sets up the class that reads and writes the Users table.
     */
    public UserService(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
        this.userDAO = new UserDAO(dbConn);
    }

    /**
     * Saves a new student if their details are ok.
     * Username must be at least 3 characters and the password at least 4.
     * If the username is already used, nothing is saved.
     * When it works, their own copy of the school database is made.
     */
    public boolean registerUser(String username, String password, String fullName) {
        // username can't be blank or shorter than 3 characters
        if (username == null || username.trim().length() < 3) {
            return false;
        }
        if (password == null || password.trim().length() < 4) {
            return false;
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }

        // don't let two people have the same username
        if (userDAO.isUsernameTaken(username)) {
            return false;
        }

        // new students start on question 1 with 0 points
        User newUser = new User(username.trim(), password, fullName.trim());

        boolean saved = userDAO.registerUser(newUser);

        // copy the clean school database into their folder
        if (saved) {
            dbConn.restoreUserDatabase(username.trim());
        }

        return saved;
    }

    /**
     * Checks the username and password.
     * Returns the student if they match a row in Users, otherwise null.
     * Also makes sure their school database copy is there before they start.
     */
    public User login(String username, String password) {
        // nothing to check if they left a box empty
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        User user = userDAO.login(username.trim(), password);

        // if the login worked, put a fresh copy of the school database in their folder
        if (user != null) {
            dbConn.restoreUserDatabase(user.getUsername());
        }

        return user;
    }

    /**
     * Returns true if that username is already in the Users table.
     */
    public boolean isUsernameTaken(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userDAO.isUsernameTaken(username.trim());
    }

    /**
     * Saves the question they are on and their new score.
     * Updates the User object and the Users table.
     */
    public boolean updateProgress(User user, int currentQuestionId, int newScore) {
        if (user == null) {
            return false;
        }

        // keep the object up to date so the screen shows the new score straight away
        user.setCurrentQuestionId(currentQuestionId);
        user.setScore(newScore);

        return userDAO.updateProgress(user.getId(), currentQuestionId, newScore);
    }

    /**
     * Clears the Users table. Only used when I was testing registration.
     */
    public void resetAllUsers() {
        userDAO.deleteAllUsers();
    }
}