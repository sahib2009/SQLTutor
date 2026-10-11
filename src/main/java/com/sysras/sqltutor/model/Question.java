package com.sysras.sqltutor.model;

/**
 * One question in the tutor.
 * Stores the topic, the question, the correct SQL, a hint and how hard it is.
 */
public class Question {
    private int id;
    private String topic;
    private String questionText;
    private String correctAnswer;
    private String hint;
    private String difficulty;

    public Question() {
        this.difficulty = "Easy";
    }

    public Question(int id, String topic, String questionText, String correctAnswer, String hint) {
        this.id = id;
        this.topic = topic;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.hint = hint;
        this.difficulty = "Normal";
    }

    public Question(int id, String topic, String questionText, String correctAnswer, String hint, String difficulty) {
        this.id = id;
        this.topic = topic;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.hint = hint;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExpectedSQL() {
        return correctAnswer;
    }

    public void setExpectedSQL(String expectedSQL) {
        this.correctAnswer = expectedSQL;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        return "Q" + id + ": " + topic + " (" + difficulty + ")";
    }
}