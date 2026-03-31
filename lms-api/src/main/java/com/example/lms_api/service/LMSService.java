package com.example.lms_api.service;

import com.example.lms_api.model.*;
import com.example.lms_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LMSService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    // F1 - Retrieve Student Enrollments: Return a list of all course enrollments for a specific student.
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }

        return enrollmentRepository.findByStudentId(studentId);
    }

    // F2 - List Active Students: Return a list of students currently enrolled in courses.
    public List<Student> getActiveStudents() {
        return enrollmentRepository.findAll().stream()
                .map(Enrollment::getStudent)
                .distinct() // Remove duplicate students
                .collect(Collectors.toList());
    }

    // F3 - Identify Most Active Instructor: Determine which instructor has the highest number of student enrollments.
    public Instructor getMostActiveInstructor() {
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();
        if (allEnrollments.isEmpty()) return null;

        // Count enrollments per instructor
        Map<Instructor, Long> instructorCounts = allEnrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getCourse().getInstructor(), Collectors.counting()));

        // Find the one with max count
        return Collections.max(instructorCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // F4 - List Instructors with No Enrollments: Return a list of instructors who currently have zero students.
    public List<Instructor> getInstructorsWithNoStudents() {
        // Get all instructors from the database
        List<Instructor> allInstructors = instructorRepository.findAll();

        // Identify IDs of instructors who HAVE student enrollments
        Set<Long> activeInstructorIds = enrollmentRepository.findAll().stream()
                .map(e -> e.getCourse().getInstructor().getId())
                .collect(Collectors.toSet());

        // Filter out instructors who are NOT in the active set
        return allInstructors.stream()
                .filter(instructor -> !activeInstructorIds.contains(instructor.getId()))
                .collect(Collectors.toList());
    }

}
