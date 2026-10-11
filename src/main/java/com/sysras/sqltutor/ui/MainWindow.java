package com.sysras.sqltutor.ui;

import com.sysras.sqltutor.model.Question;
import com.sysras.sqltutor.model.QueryResult;
import com.sysras.sqltutor.model.User;
import com.sysras.sqltutor.service.QuestionService;
import com.sysras.sqltutor.service.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * The main screen. Questions are listed on the left.
 * The right side shows the question, the SQL box and the results table.
 *
 * @author Sahib Singh
 */
public class MainWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    // who is logged in, and the classes that talk to the database
    private UserService userService;
    private QuestionService questionService;
    private User currentUser;

    // search box and the list of questions on the left
    private JTextField txtSearch;
    private DefaultListModel<Question> questionListModel;
    private JList<Question> questionList;
    private JLabel lblUserScore;

    // the question that is open
    private JLabel lblTopicTitle;
    private JLabel lblDifficulty;
    private JTextArea txtQuestionPrompt;
    private Question currentQuestion;

    // where the student types their SQL
    private JTextArea txtSqlEditor;

    // the message under the SQL box and the results table
    private JLabel lblStatusMessage;
    private JTable tblResults;
    private DefaultTableModel emptyTableModel;

    public MainWindow(UserService userService, QuestionService questionService, User user) {
        this.userService = userService;
        this.questionService = questionService;
        this.currentUser = user;

        setTitle("SQL Tutor - " + user.getFullName() + " (" + user.getUsername() + ")");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadAllQuestions();

        // open the question they were on last time, so they don't start from the beginning
        selectQuestionById(user.getCurrentQuestionId());

        setVisible(true);
    }

    /**
     * Puts the question list on the left and the question screen on the right.
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // left side has the search box and the questions
        JPanel leftPanel = createLeftNavigationPanel();

        // right side has the question, the SQL box and the results
        JPanel rightPanel = createRightContentPanel();

        // split pane so the student can drag the bar if the list is too narrow
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(280);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Left panel: search at the top, questions in the middle, score at the bottom.
     */
    private JPanel createLeftNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // search box
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Topics"));

        txtSearch = new JTextField();
        txtSearch.setToolTipText("Search by topic name or keyword");

        JPanel searchBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        JButton btnSearch = new JButton("Search");
        JButton btnClearSearch = new JButton("Reset");

        btnSearch.addActionListener(e -> onSearch());
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadAllQuestions();
        });

        searchBtnRow.add(btnSearch);
        searchBtnRow.add(btnClearSearch);

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(searchBtnRow, BorderLayout.SOUTH);

        panel.add(searchPanel, BorderLayout.NORTH);

        // the list of questions. Clicking one shows it on the right
        questionListModel = new DefaultListModel<>();
        questionList = new JList<>(questionListModel);
        questionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionList.setFont(new Font("SansSerif", Font.PLAIN, 12));

        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Question selected = questionList.getSelectedValue();
                if (selected != null) {
                    displayQuestion(selected);
                }
            }
        });

        JScrollPane scrollList = new JScrollPane(questionList);
        scrollList.setBorder(BorderFactory.createTitledBorder("Course Questions"));
        panel.add(scrollList, BorderLayout.CENTER);

        // name, score and the log out button
        JPanel profilePanel = new JPanel(new GridLayout(3, 1, 3, 3));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Student Progress"));

        JLabel lblName = new JLabel("Learner: " + currentUser.getFullName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 12));

        lblUserScore = new JLabel("Current Score: " + currentUser.getScore() + " pts");
        lblUserScore.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblUserScore.setForeground(new Color(20, 120, 30));

        JButton btnLogout = new JButton("Log Out");
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginWindow(userService, questionService);
        });

        profilePanel.add(lblName);
        profilePanel.add(lblUserScore);
        profilePanel.add(btnLogout);

        panel.add(profilePanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Right panel: the question at the top, SQL in the middle, results underneath.
     */
    private JPanel createRightContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        // topic, difficulty and the actual question
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Question Details"));

        JPanel headerRow = new JPanel(new BorderLayout());
        lblTopicTitle = new JLabel("Topic: None Selected");
        lblTopicTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        lblDifficulty = new JLabel("Difficulty: -");
        lblDifficulty.setFont(new Font("SansSerif", Font.ITALIC, 12));

        headerRow.add(lblTopicTitle, BorderLayout.WEST);
        headerRow.add(lblDifficulty, BorderLayout.EAST);
        topPanel.add(headerRow, BorderLayout.NORTH);

        txtQuestionPrompt = new JTextArea(3, 40);
        txtQuestionPrompt.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtQuestionPrompt.setLineWrap(true);
        txtQuestionPrompt.setWrapStyleWord(true);
        txtQuestionPrompt.setEditable(false);
        txtQuestionPrompt.setBackground(new Color(245, 245, 245));
        topPanel.add(new JScrollPane(txtQuestionPrompt), BorderLayout.CENTER);

        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnHint = new JButton("💡 Show Hint");
        btnHint.addActionListener(e -> onShowHint());
        hintRow.add(btnHint);
        topPanel.add(hintRow, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // the box where they type the SQL
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel editorPanel = new JPanel(new BorderLayout(5, 5));
        editorPanel.setBorder(BorderFactory.createTitledBorder("SQL Query Editor (Target: Learners, Subjects, Marks)"));

        txtSqlEditor = new JTextArea(4, 40);
        txtSqlEditor.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtSqlEditor.setLineWrap(true);
        editorPanel.add(new JScrollPane(txtSqlEditor), BorderLayout.CENTER);

        // run the query, restore the database, or go to the next question
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton btnExecute = new JButton("▶ Execute Query");
        btnExecute.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExecute.setBackground(new Color(220, 240, 255));
        btnExecute.addActionListener(e -> onExecuteQuery());

        JButton btnRestoreDB = new JButton("↺ Restore Database");
        btnRestoreDB.setToolTipText("Resets your working practice database back to original school data");
        btnRestoreDB.addActionListener(e -> onRestoreDatabase());

        JButton btnPrev = new JButton("⬅ Previous");
        btnPrev.addActionListener(e -> onNavigateQuestion(-1));

        JButton btnNext = new JButton("Next ➡");
        btnNext.addActionListener(e -> onNavigateQuestion(1));

        actionRow.add(btnExecute);
        actionRow.add(btnRestoreDB);
        actionRow.add(btnPrev);
        actionRow.add(btnNext);

        editorPanel.add(actionRow, BorderLayout.SOUTH);
        centerPanel.add(editorPanel, BorderLayout.NORTH);

        // tells them if it was right, and shows the rows that came back
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Query Execution Output"));

        lblStatusMessage = new JLabel("Ready. Enter an SQL statement and click Execute.");
        lblStatusMessage.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblStatusMessage.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        resultsPanel.add(lblStatusMessage, BorderLayout.NORTH);

        emptyTableModel = new DefaultTableModel(new String[]{"Output"}, 0);
        tblResults = new JTable(emptyTableModel);
        tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblResults.setFillsViewportHeight(true);

        JScrollPane resultsScroll = new JScrollPane(tblResults);
        resultsScroll.setPreferredSize(new Dimension(600, 200));
        resultsPanel.add(resultsScroll, BorderLayout.CENTER);

        centerPanel.add(resultsPanel, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Clears the list and loads every question from the database into it.
     */
    private void loadAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        questionListModel.clear();
        for (Question q : questions) {
            questionListModel.addElement(q);
        }
        if (!questions.isEmpty() && questionList.getSelectedIndex() < 0) {
            questionList.setSelectedIndex(0);
        }
    }

    /**
     * Selects the question with this id. If it isn't in the list, the first one is selected.
     */
    private void selectQuestionById(int questionId) {
        for (int i = 0; i < questionListModel.size(); i++) {
            if (questionListModel.get(i).getId() == questionId) {
                questionList.setSelectedIndex(i);
                return;
            }
        }
        if (!questionListModel.isEmpty()) {
            questionList.setSelectedIndex(0);
        }
    }

    /**
     * Shows the topic, difficulty and question text, and clears the old SQL and results.
     */
    private void displayQuestion(Question q) {
        this.currentQuestion = q;
        lblTopicTitle.setText("Topic: " + q.getTopic());
        lblDifficulty.setText("Difficulty: " + q.getDifficulty());
        txtQuestionPrompt.setText(q.getQuestionText());
        txtSqlEditor.setText("");
        lblStatusMessage.setText("Ready. Enter your query above.");
        lblStatusMessage.setForeground(Color.BLACK);
        tblResults.setModel(emptyTableModel);
    }

    /**
     * Runs the SQL in the text box and shows whether it was right.
     */
    private void onExecuteQuery() {
        if (currentQuestion == null) {
            JOptionPane.showMessageDialog(this, "Please select a question first.", "No Question", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userSQL = txtSqlEditor.getText().trim();
        if (userSQL.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write an SQL query in the editor box.", "Empty Query", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // QuestionService runs the query and marks it
        QueryResult result = questionService.executeSQL(currentUser, currentQuestion, userSQL);

        // put the rows into the table on the screen
        if (result.getTableModel() != null) {
            tblResults.setModel(result.getTableModel());
        } else {
            tblResults.setModel(emptyTableModel);
        }

        // green message and a new score if they got it, red if they didn't
        if (result.isCorrect()) {
            lblStatusMessage.setText("✓ " + result.getMessage());
            lblStatusMessage.setForeground(new Color(0, 130, 30));
            lblUserScore.setText("Current Score: " + currentUser.getScore() + " pts");
            JOptionPane.showMessageDialog(this,
                    result.getMessage() + "\nScore: " + currentUser.getScore() + " points.",
                    "Question Completed!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblStatusMessage.setText("✗ " + result.getMessage());
            lblStatusMessage.setForeground(new Color(180, 0, 0));
        }
    }

    /**
     * Pops up the hint for the question that is open.
     */
    private void onShowHint() {
        if (currentQuestion != null) {
            String hint = currentQuestion.getHint();
            if (hint == null || hint.trim().isEmpty()) {
                hint = "No hint available for this question. Review your SQL notes!";
            }
            JOptionPane.showMessageDialog(this,
                    hint,
                    "Hint for: " + currentQuestion.getTopic(),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Asks first, then copies the master school database over their working copy.
     */
    private void onRestoreDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to restore your practice database?\nAll tables will be reset to the original school data.",
                "Confirm Database Restore",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean restored = questionService.restoreUserDatabase(currentUser.getUsername());
            if (restored) {
                lblStatusMessage.setText("Database restored to fresh master copy.");
                lblStatusMessage.setForeground(new Color(0, 100, 180));
                JOptionPane.showMessageDialog(this,
                        "Your practice database has been successfully restored to the original master copy.",
                        "Database Restored",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to restore database. Please check master file.",
                        "Restore Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Filters the question list using whatever is in the search box.
     */
    private void onSearch() {
        String keyword = txtSearch.getText().trim();
        List<Question> matches = questionService.searchQuestions(keyword);
        questionListModel.clear();
        for (Question q : matches) {
            questionListModel.addElement(q);
        }
        if (!matches.isEmpty()) {
            questionList.setSelectedIndex(0);
        } else {
            lblStatusMessage.setText("No questions found matching: '" + keyword + "'");
            lblStatusMessage.setForeground(Color.RED);
        }
    }

    /**
     * Moves one question up or down the list. Does nothing if you're already at the end.
     */
    private void onNavigateQuestion(int delta) {
        int currentIndex = questionList.getSelectedIndex();
        int newIndex = currentIndex + delta;
        if (newIndex >= 0 && newIndex < questionListModel.size()) {
            questionList.setSelectedIndex(newIndex);
        }
    }
}