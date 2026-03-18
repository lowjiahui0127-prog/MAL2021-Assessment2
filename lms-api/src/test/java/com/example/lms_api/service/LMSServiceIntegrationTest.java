package com.example.lms_api.service;

import com.example.lms_api.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class LMSServiceIntegrationTest {
    @Autowired
    private LMSService lmsService;

    @Test
    void testF1_GetStudentEnrollments_Integration() {
        // Arrange: Use ID from DataInitializer (Assuming ID 1 is JiaHui)
        Long studentId = 1L;

        // Act: Retrieve enrollments from the real database
        List<Enrollment> enrollments = lmsService.getStudentEnrollments(studentId);

        // Assert: Verify data from DataInitializer
        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty(), "JiaHui should have at least one enrollment");
        assertEquals("MAL2021 Software Development Tools and Practices",
                enrollments.get(0).getCourse().getCourseName());
    }

    @Test
    void testF2_GetActiveStudents_Integration() {
        // Act: Get list of students currently enrolled in courses
        List<Student> activeStudents = lmsService.getActiveStudents();

        // Assert: Only JiaHui is enrolled in DataInitializer
        assertEquals(1, activeStudents.size(), "Should have exactly 1 active student");
        assertEquals("JiaHui", activeStudents.get(0).getName());
    }

    @Test
    void testF3_GetMostActiveInstructor_Integration() {
        // Act: Identify instructor with highest enrollment
        Instructor mostActive = lmsService.getMostActiveInstructor();

        // Assert: Ms. Grace has 1 student, Mr. Eric has 0
        assertNotNull(mostActive);
        assertEquals("Ms. Grace", mostActive.getName());
    }

    @Test
    void testF4_GetInstructorsWithNoStudents_Integration() {
        // Act: List instructors with zero enrollments
        List<Instructor> inactiveInstructors = lmsService.getInstructorsWithNoStudents();

        // Assert: Mr. Eric should be the only one with no students
        assertNotNull(inactiveInstructors);
        assertEquals(1, inactiveInstructors.size());
        assertEquals("Mr. Eric", inactiveInstructors.get(0).getName());
    }
}
