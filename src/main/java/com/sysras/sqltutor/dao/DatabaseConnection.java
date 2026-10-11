package com.sysras.sqltutor.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to the two Access databases.
 * SQLTutor.accdb keeps the students and questions.
 * Each student also gets their own copy of the school database to practise on.
 *
 * @author Sahib Singh
 */
public class DatabaseConnection {

    // folders the database files are kept in
    private static final String MASTER_DIR = "master";
    private static final String USERS_DIR = "users";

    /**
     * Loads the UCanAccess driver so Java can open .accdb files.
     */
    public DatabaseConnection() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess JDBC driver not found: " + e.getMessage());
        }
    }

    /**
     * Finds a database in the master folder.
     * NetBeans sometimes runs from the project folder and sometimes from
     * the folder above it, so I check both places.
     */
    public static File getMasterFile(String filename) {
        File direct = new File(MASTER_DIR, filename);
        if (direct.exists()) {
            return direct;
        }
        File nested = new File("Code/SQLTutor/master", filename);
        if (nested.exists()) {
            return nested;
        }
        return direct;
    }

    /**
     * Works out the path of this student's own school database.
     * The file is named School_ and then their username, inside users/.
     * Spaces and weird characters in the username get swapped for _.
     */
    public static File getUserWorkingFile(String username) {
        String cleanName = username.trim().replaceAll("[^a-zA-Z0-9_]", "_");
        File direct = new File(USERS_DIR, cleanName);
        if (!direct.exists()) {
            direct.mkdirs();
        }
        return new File(direct, "School_" + cleanName + ".accdb");
    }

    /**
     * Opens SQLTutor.accdb. That file has the Users and Questions tables.
     */
    public Connection getTutorConnection() throws SQLException {
        File tutorFile = getMasterFile("SQLTutor.accdb");
        String url = "jdbc:ucanaccess://" + tutorFile.getAbsolutePath().replace('\\', '/');
        return DriverManager.getConnection(url);
    }

    /**
     * Opens the student's own copy of the school database.
     * If they don't have one yet, it gets copied from the master first.
     */
    public Connection getUserWorkingConnection(String username) throws SQLException {
        File userFile = getUserWorkingFile(username);
        if (!userFile.exists()) {
            restoreUserDatabase(username);
        }
        String url = "jdbc:ucanaccess://" + userFile.getAbsolutePath().replace('\\', '/');
        return DriverManager.getConnection(url);
    }

    /**
     * Copies SchoolMaster.accdb over the student's file so the tables
     * are back to the original data. Returns false if the master file is missing.
     */
    public boolean restoreUserDatabase(String username) {
        File masterFile = getMasterFile("SchoolMaster.accdb");
        File userFile = getUserWorkingFile(username);

        if (!masterFile.exists()) {
            System.err.println("Master tutorial database not found at: " + masterFile.getAbsolutePath());
            return false;
        }

        try {
            // make the student's folder if it isn't there yet
            if (userFile.getParentFile() != null && !userFile.getParentFile().exists()) {
                userFile.getParentFile().mkdirs();
            }

            // overwrite their old copy with the clean master
            Files.copy(masterFile.toPath(), userFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error restoring user database: " + e.getMessage());
            return false;
        }
    }
}