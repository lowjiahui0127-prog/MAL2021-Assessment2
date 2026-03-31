package com.example.lms_api.gui;

import com.example.lms_api.model.Enrollment;
import org.springframework.web.client.RestTemplate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LMSAdminGUI extends JFrame {

    private static final String BASE_URL = "http://localhost:8080/api/lms/students/";

    private JTextField studentIdField;
    private JButton searchButton;
    private JLabel studentInfoLabel;
    private DefaultTableModel tableModel;
    private JTable enrollmentTable;
    private JLabel statusLabel;

    public LMSAdminGUI() {
        setTitle("LMS Admin - Student Enrollment Lookup");
        setSize(720, 500);
        setMinimumSize(new Dimension(580, 380));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));

        // Top area (header + search + student info)
        JPanel topArea = new JPanel(new BorderLayout(0, 0));

        // Header
        JLabel header = new JLabel("Student Enrollment Lookup", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(new Color(25, 75, 145));
        header.setBorder(new EmptyBorder(14, 0, 6, 0));
        topArea.add(header, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        searchPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));

        JLabel idLabel = new JLabel("Student ID:");
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        studentIdField = new JTextField(10);
        studentIdField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        studentIdField.setToolTipText("Enter numeric Student ID (e.g. 1, 2, 3)");
        studentIdField.addActionListener(e -> performSearch()); // Mod 1: Enter key triggers search

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchButton.addActionListener(e -> performSearch());

        searchPanel.add(idLabel);
        searchPanel.add(studentIdField);
        searchPanel.add(searchButton);
        topArea.add(searchPanel, BorderLayout.CENTER);

        // Student info panel
        studentInfoLabel = new JLabel("  No student loaded.", SwingConstants.LEFT);
        studentInfoLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        studentInfoLabel.setForeground(Color.GRAY);
        studentInfoLabel.setBorder(new EmptyBorder(4, 12, 4, 12));
        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(new Color(245, 248, 255));
        infoBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 210, 210)));
        infoBar.add(studentInfoLabel, BorderLayout.WEST);
        topArea.add(infoBar, BorderLayout.SOUTH);

        add(topArea, BorderLayout.NORTH);

        // Table
        String[] columns = {"Enrollment ID", "Course Name", "Instructor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // Modification 7
            }
        };
        enrollmentTable = new JTable(tableModel);
        enrollmentTable.setRowHeight(26);
        enrollmentTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        enrollmentTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        enrollmentTable.setSelectionBackground(new Color(173, 214, 255));
        enrollmentTable.setGridColor(new Color(220, 220, 220));
        enrollmentTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        enrollmentTable.getColumnModel().getColumn(1).setPreferredWidth(390);
        enrollmentTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);

        // status bar with count and timestamp
        statusLabel = new JLabel("  Enter a Student ID above and press Search.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(new EmptyBorder(4, 6, 4, 6));
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void performSearch() {
        String input = studentIdField.getText().trim();

        // input validation
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Student ID.", "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long studentId;
        try {
            studentId = Long.parseLong(input);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Student ID must be a number (e.g. 1, 2, 3).",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            studentIdField.selectAll();
            return;
        }

        // disable button during fetch
        searchButton.setEnabled(false);
        searchButton.setText("Searching…");
        studentInfoLabel.setText("  Searching…");
        studentInfoLabel.setForeground(Color.DARK_GRAY);
        setStatus("Searching for Student ID " + studentId + "…", Color.DARK_GRAY);

        final long id = studentId;
        SwingWorker<Enrollment[], Void> worker = new SwingWorker<>() {
            @Override
            protected Enrollment[] doInBackground() {
                RestTemplate rt = new RestTemplate();
                return rt.getForObject(BASE_URL + id + "/enrollments", Enrollment[].class);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                searchButton.setText("Search");
                try {
                    Enrollment[] enrollments = get();
                    tableModel.setRowCount(0);

                    if (enrollments != null && enrollments.length > 0) {
                        // show student name + email from first record
                        var student = enrollments[0].getStudent();
                        if (student != null) {
                            studentInfoLabel.setText("  Student: " + student.getName() + "   |   Email: " + student.getEmail());
                            studentInfoLabel.setForeground(new Color(30, 80, 150));
                        }

                        for (Enrollment e : enrollments) {
                            String courseName     = e.getCourse() != null ? e.getCourse().getCourseName() : "N/A";
                            String instructorName = (e.getCourse() != null && e.getCourse().getInstructor() != null)
                                    ? e.getCourse().getInstructor().getName() : "N/A";
                            tableModel.addRow(new Object[]{e.getId(), courseName, instructorName});
                        }
                        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        setStatus("  Found " + enrollments.length + " enrollment(s) for Student ID " + id + "   |   Last searched: " + time, new Color(0, 120, 0));

                    } else {
                        studentInfoLabel.setText("  Student ID " + id + " - no enrollments found.");
                        studentInfoLabel.setForeground(new Color(160, 90, 0));
                        setStatus("  Student ID " + id + " is not currently enrolled in any course.", new Color(180, 100, 0));
                    }

                } catch (Exception ex) {
                    studentInfoLabel.setText("  Connection error.");
                    studentInfoLabel.setForeground(Color.RED);
                    String msg = "Cannot connect to server. Is the Spring Boot application running on port 8080?";
                    setStatus("  Error: " + msg, Color.RED);
                    JOptionPane.showMessageDialog(LMSAdminGUI.this,
                            msg, "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(LMSAdminGUI::new);
    }
}
