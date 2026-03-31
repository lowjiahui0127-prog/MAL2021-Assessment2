package com.example.lms_api.service;

import com.example.lms_api.model.*;
import com.example.lms_api.repository.CourseRepository;
import com.example.lms_api.repository.EnrollmentRepository;
import com.example.lms_api.repository.InstructorRepository;
import com.example.lms_api.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class LMSServiceIntegrationTest {
    @Autowired
    private LMSService lmsService;

    // Repositories used to insert controlled test data
    @Autowired private StudentRepository studentRepo;
    @Autowired private InstructorRepository instructorRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    /**
     * Integration test for F1 (retrieve student enrollments) and F2 (list active students) running against the real H2 database.
     */
    @Test
    void testRetrieveAndListActiveStudents() {
        // Arrange: insert test data directly via repositories
        Instructor grace = instructorRepo.save(new Instructor("Ms. Grace", "grace@test.com"));
        Student jiahui = studentRepo.save(new Student("JiaHui", "jiahui@test.com"));
        Student sulyn = studentRepo.save(new Student("Sulyn", "sulyn@test.com")); // not enrolled
        Course courseMAL = courseRepo.save(new Course("MAL2021 Software Development Tools and Practices", grace));
        enrollmentRepo.save(new Enrollment(jiahui, courseMAL));

        // F1: retrieve enrollments for JiaHui using the actual DB-generated ID
        List<Enrollment> enrollments = lmsService.getStudentEnrollments(jiahui.getId());
        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty(), "JiaHui should have at least one enrollment");
        assertEquals("MAL2021 Software Development Tools and Practices",
                enrollments.get(0).getCourse().getCourseName(), "The enrolled course name should match");

        // F1: Sulyn is not enrolled - should return an empty list (not null, not error)
        List<Enrollment> sylynEnrollments = lmsService.getStudentEnrollments(sulyn.getId());
        assertNotNull(sylynEnrollments);
        assertTrue(sylynEnrollments.isEmpty(), "Sulyn is not enrolled in any course - F1 must return empty list");

        // F2: only JiaHui is enrolled; Sulyn must be excluded
        List<Student> activeStudents = lmsService.getActiveStudents();
        assertEquals(1, activeStudents.size(), "Only JiaHui should be returned as an active student");
        assertEquals("JiaHui", activeStudents.get(0).getName());
    }

    /**
     * Integration test for F3 (most active instructor) and F4 (instructors with no enrollments) running against the real H2 database.
     */
    @Test
    void testIdentifyActiveAndInactiveInstructors() {
        // Arrange
        Instructor grace = instructorRepo.save(new Instructor("Ms. Grace", "grace@test.com"));
        Instructor eric  = instructorRepo.save(new Instructor("Mr. Eric",  "eric@test.com"));
        Instructor amir  = instructorRepo.save(new Instructor("Mr. Amir",  "amir@test.com"));

        Student jiahui = studentRepo.save(new Student("JiaHui", "jiahui@test.com"));
        Student celine = studentRepo.save(new Student("Celine",  "celine@test.com"));

        Course cGrace1 = courseRepo.save(new Course("MAL2021 Software Development Tools and Practices", grace));
        Course cGrace2 = courseRepo.save(new Course("MAL2020 Computing Group Project", grace));
        courseRepo.save(new Course("MAL2019 Artificial Intelligence", eric)); // Eric has a course but no students
        // amir has no course at all

        // Grace gets 3 total enrollments; Eric and Amir get 0
        enrollmentRepo.save(new Enrollment(jiahui, cGrace1));
        enrollmentRepo.save(new Enrollment(jiahui, cGrace2));
        enrollmentRepo.save(new Enrollment(celine, cGrace1));

        // F3: Grace has 3 enrollments across 2 courses (most active)
        Instructor mostActive = lmsService.getMostActiveInstructor();
        assertNotNull(mostActive);
        assertEquals("Ms. Grace", mostActive.getName(), "Ms. Grace has the most total enrollments and should be most active");

        // F4: Eric (has course, 0 enrollments) AND Amir (no course) must both appear
        List<Instructor> inactive = lmsService.getInstructorsWithNoStudents();
        assertNotNull(inactive);
        assertEquals(2, inactive.size(), "Both Eric and Amir should have no enrollments");
        assertTrue(inactive.stream().anyMatch(i -> i.getName().equals("Mr. Eric")),
                "Mr. Eric has a course but no enrolled students - must appear in F4");
        assertTrue(inactive.stream().anyMatch(i -> i.getName().equals("Mr. Amir")),
                "Mr. Amir has no course at all - must also appear in F4");
    }
}
