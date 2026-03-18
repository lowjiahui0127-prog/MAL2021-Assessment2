package com.example.lms_api.gui;

import com.example.lms_api.model.Student;
import org.springframework.web.client.RestTemplate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class LMSAdminGUI extends JFrame {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;

    public LMSAdminGUI() {
        setTitle("LMS Admin Dashboard - Active Students");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout
        setLayout(new BorderLayout());

        // Table
        String[] columns = {"ID", "Name", "Email"};
        tableModel = new DefaultTableModel(columns, 0);
        studentTable = new JTable(tableModel);
        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Button
        refreshButton = new JButton("Load Active Students");
        add(refreshButton, BorderLayout.SOUTH);

        // Action Listener
        refreshButton.addActionListener(e -> fetchActiveStudents());

        setVisible(true);
    }

    private void fetchActiveStudents() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/api/lms/students/active";
            Student[] students = restTemplate.getForObject(url, Student[].class);

            tableModel.setRowCount(0); // Clear table
            if (students != null) {
                for (Student s : students) {
                    tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getEmail()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Run the GUI
        SwingUtilities.invokeLater(LMSAdminGUI::new);
    }
}
