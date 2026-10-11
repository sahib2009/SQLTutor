package com.sysras.sqltutor.model;

/**
 * A student who uses the app. Has their username, password, and how far they've gotten.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private int currentQuestionId;
    private int score;

    // Default constructor
    public User() {
        this.currentQuestionId = 1;
        this.score = 0;
    }

    // Overloaded constructor for creating a new student account
    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.currentQuestionId = 1;
        this.score = 0;
    }

    // Constructor with everything - used when loading a student from the database
    public User(int id, String username, String password, String fullName, int currentQuestionId, int score) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.currentQuestionId = currentQuestionId;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(int currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ") - Score: " + score;
    }
}