package com.sysras.sqltutor.model;

import javax.swing.table.DefaultTableModel;

/**
 * What comes back after a student runs a query.
 * Says if they got it right, the message to show them,
 * the table of results, and how many rows changed.
 */
public class QueryResult {
    private boolean correct;
    private String message;
    private DefaultTableModel tableModel;
    private int rowsAffected;

    public QueryResult() {
        this.correct = false;
        this.message = "";
        this.tableModel = null;
        this.rowsAffected = 0;
    }

    public QueryResult(boolean correct, String message) {
        this.correct = correct;
        this.message = message;
        this.tableModel = null;
        this.rowsAffected = 0;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public int getRowsAffected() {
        return rowsAffected;
    }

    public void setRowsAffected(int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }
}
