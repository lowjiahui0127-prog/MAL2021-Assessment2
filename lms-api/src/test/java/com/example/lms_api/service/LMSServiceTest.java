package com.example.lms_api.service;

import com.example.lms_api.model.*;
import com.example.lms_api.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LMSServiceTest {
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @InjectMocks
    private LMSService lmsService;

    @BeforeEach
    void setup() {
        // Initialize Mockito annotations before each test
        MockitoAnnotations.openMocks(this);
    }

    // ── F1: Retrieve Student Enrollments ─────────────────────────────────────

    /**
     * Normal case: student is enrolled in one course.
     * Verifies the service delegates to enrollmentRepository.findByStudentId() and returns the result unchanged.
     */
    @Test
    void testF1_GetStudentEnrollments_ReturnsEnrollments() {
        // Arrange: Setup mock conditions
        Long studentId = 1L;
        List<Enrollment> mockEnrollments = Collections.singletonList(new Enrollment());
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(mockEnrollments);

        // Act: Call the service method
        List<Enrollment> result = lmsService.getStudentEnrollments(studentId);

        // Assert: Verify the result and interaction
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enrollmentRepository, times(1)).findByStudentId(studentId);
    }

    /**
     * Normal case: student is enrolled in multiple courses.
     * Verifies all enrollments are returned (not just the first one).
     */
    @Test
    void testF1_GetStudentEnrollments_MultipleEnrollments_ReturnsAll() {
        Long studentId = 1L;
        List<Enrollment> mockData = Arrays.asList(new Enrollment(), new Enrollment());
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(mockData);

        List<Enrollment> result = lmsService.getStudentEnrollments(studentId);

        assertEquals(2, result.size(), "All enrollments should be returned");
        verify(enrollmentRepository, times(1)).findByStudentId(studentId);
    }

    /**
     * Edge case: student exists but is not enrolled in any course.
     * F1 should return an empty list - not null, not an error.
     */
    @Test
    void testF1_GetStudentEnrollments_NotEnrolled_ReturnsEmptyList() {
        when(enrollmentRepository.findByStudentId(99L)).thenReturn(Collections.emptyList());

        List<Enrollment> result = lmsService.getStudentEnrollments(99L);

        assertNotNull(result, "Result should never be null");
        assertTrue(result.isEmpty(), "Student with no enrollments should return empty list");
    }

    /**
     * Edge case: null studentId passed.
     * Service should return empty list without calling the repository.
     */
    @Test
    void testF1_GetStudentEnrollments_NullId_ReturnsEmptyListWithoutCallingRepo() {
        List<Enrollment> result = lmsService.getStudentEnrollments(null);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Null ID should return empty list");
        verify(enrollmentRepository, never()).findByStudentId(any());
    }

    // ── F2: List Active Students ─────────────────────────────────────────────

    /**
     * Normal case: one student enrolled in two courses.
     * distinct() must deduplicate - only 1 student should be returned.
     */
    @Test
    void testF2_GetActiveStudents_DeduplicatesSameStudent() {
        // Arrange: Create duplicate student enrollments to test distinct logic
        Student student = new Student("JiaHui", "bsse2509252@peninsulamalaysia.edu.my");
        Enrollment e1 = new Enrollment(); e1.setStudent(student);
        Enrollment e2 = new Enrollment(); e2.setStudent(student);
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        // Act: Execute the method
        List<Student> activeStudents = lmsService.getActiveStudents();

        // Assert: Check if only 1 unique student is returned
        assertEquals(1, activeStudents.size(), "Same student in multiple courses must appear only once");
        assertEquals("JiaHui", activeStudents.get(0).getName());
    }

    /**
     * Normal case: two different students each enrolled in one course.
     * Both should appear in the result.
     */
    @Test
    void testF2_GetActiveStudents_MultipleStudents_ReturnsAll() {
        Student s1 = new Student("JiaHui", "jiahui@test.com");
        Student s2 = new Student("Celine", "celine@test.com");
        Enrollment e1 = new Enrollment(); e1.setStudent(s1);
        Enrollment e2 = new Enrollment(); e2.setStudent(s2);
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<Student> result = lmsService.getActiveStudents();

        assertEquals(2, result.size(), "Two distinct students should both be returned");
    }

    /**
     * Edge case: no enrollments exist at all.
     * F2 should return an empty list - not null.
     */
    @Test
    void testF2_GetActiveStudents_NoEnrollments_ReturnsEmptyList() {
        when(enrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        List<Student> result = lmsService.getActiveStudents();

        assertNotNull(result, "Result should never be null");
        assertTrue(result.isEmpty(), "No enrollments means no active students");
    }

    // ── F3: Identify Most Active Instructor ──────────────────────────────────

    /**
     * Normal case: Grace has 3 enrollments, Eric has 1.
     * Grace should be identified as most active (by enrollment count, not course count).
     */
    @Test
    void testF3_GetMostActiveInstructor() {
        // Arrange: Setup Instructors and Enrollments
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        Instructor profEric = new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my");

        Course c1 = new Course("MAL2021 Software Development Tools and Practices", profGrace);
        Course c2 = new Course("MAL2020 Computing Group Project", profGrace);
        Course c3 = new Course("MAL2019 Artificial Intelligence", profEric);

        // Grace: 2 enrollments across 2 courses; Eric: 1 enrollment in 1 course
        Enrollment e1 = new Enrollment(); e1.setCourse(c1); // Grace +1
        Enrollment e2 = new Enrollment(); e2.setCourse(c2); // Grace +2
        Enrollment e3 = new Enrollment(); e3.setCourse(c3); // Eric +1

        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2, e3));

        // Act
        Instructor mostActive = lmsService.getMostActiveInstructor();

        // Assert
        assertNotNull(mostActive);
        assertEquals("Ms. Grace", mostActive.getName(), "Ms. Grace has more enrollments");
    }

    /**
     * Normal case: Grace teaches 1 course with 3 enrollments; Eric teaches 2 courses with 1 each.
     * Demonstrates that F3 counts ENROLLMENT TOTAL, not number of courses.
     * Grace (1 course, 3 enrollments) beats Eric (2 courses, 2 enrollments).
     */
    @Test
    void testF3_GetMostActiveInstructor_CountsEnrollmentsTotalNotCourseCount() {
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        Instructor profEric  = new Instructor("Mr. Eric",  "eric@peninsulamalaysia.edu.my");

        Course c1 = new Course("MAL2021", profGrace); // Grace: 1 course
        Course c2 = new Course("MAL2019", profEric);  // Eric:  2 courses
        Course c3 = new Course("MAL2018", profEric);

        // Grace: 3 students in 1 course = 3 enrollments
        Enrollment e1 = new Enrollment(); e1.setCourse(c1);
        Enrollment e2 = new Enrollment(); e2.setCourse(c1);
        Enrollment e3 = new Enrollment(); e3.setCourse(c1);
        // Eric: 1 student each in 2 courses = 2 enrollments
        Enrollment e4 = new Enrollment(); e4.setCourse(c2);
        Enrollment e5 = new Enrollment(); e5.setCourse(c3);

        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2, e3, e4, e5));

        Instructor result = lmsService.getMostActiveInstructor();

        assertEquals("Ms. Grace", result.getName(), "F3 must count total enrollments (3 > 2), not number of courses (1 < 2)");
    }

    /**
     * Edge case: no enrollments at all.
     * F3 should return null - there is no most active instructor.
     */
    @Test
    void testF3_GetMostActiveInstructor_NoEnrollments_ReturnsNull() {
        when(enrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        Instructor result = lmsService.getMostActiveInstructor();

        assertNull(result, "With no enrollments there is no most active instructor");
    }

    /**
     * Corner case: only one instructor with one enrollment.
     * F3 should return that single instructor.
     */
    @Test
    void testF3_GetMostActiveInstructor_SingleInstructor_ReturnsThem() {
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        Course c1 = new Course("MAL2021", profGrace);
        Enrollment e1 = new Enrollment(); e1.setCourse(c1);

        when(enrollmentRepository.findAll()).thenReturn(Collections.singletonList(e1));

        Instructor result = lmsService.getMostActiveInstructor();

        assertNotNull(result);
        assertEquals("Ms. Grace", result.getName(), "Single instructor should be returned as most active");
    }

    // ── F4: List Instructors with No Enrollments ─────────────────────────────

    /**
     * Normal case: covers both F4 scenarios
     *   Eric: has a course but nobody enrolled (has course, 0 students)
     *   Amir: no course assigned at all (newly hired / not yet assigned)
     * Both must appear in F4 result. Grace must NOT appear (she has enrollments).
     */
    @Test
    void testF4_GetInstructorsWithNoStudents_BothScenarios() {
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        profGrace.setId(1L);
        Instructor profEric = new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my");
        profEric.setId(2L);
        Instructor profAmir = new Instructor("Mr. Amir", "amir@peninsulamalaysia.edu.my");
        profAmir.setId(3L);

        // Grace has a course with 1 enrollment → active, must NOT appear
        Course c1 = new Course("MAL2021", profGrace);
        Enrollment e1 = new Enrollment(); e1.setCourse(c1);
        // Eric has a course but 0 enrollments → F4
        // Amir has no course at all → F4

        when(instructorRepository.findAll()).thenReturn(Arrays.asList(profGrace, profEric, profAmir));
        when(enrollmentRepository.findAll()).thenReturn(Collections.singletonList(e1));

        List<Instructor> result = lmsService.getInstructorsWithNoStudents();

        assertEquals(2, result.size(), "Eric and Amir should both have no enrollments");
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Mr. Eric")),
                "Mr. Eric (has course, no students) must appear in F4");
        assertTrue(result.stream().anyMatch(i -> i.getName().equals("Mr. Amir")),
                "Mr. Amir (no course at all) must also appear in F4");
        assertFalse(result.stream().anyMatch(i -> i.getName().equals("Ms. Grace")),
                "Ms. Grace has active enrollments and must NOT appear in F4");
    }

    /**
     * Edge case: every instructor has at least one enrollment.
     * F4 should return an empty list.
     */
    @Test
    void testF4_GetInstructorsWithNoStudents_AllActive_ReturnsEmptyList() {
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        profGrace.setId(1L);
        Course c1 = new Course("MAL2021", profGrace);
        Enrollment e1 = new Enrollment(); e1.setCourse(c1);

        when(instructorRepository.findAll()).thenReturn(Collections.singletonList(profGrace));
        when(enrollmentRepository.findAll()).thenReturn(Collections.singletonList(e1));

        List<Instructor> result = lmsService.getInstructorsWithNoStudents();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "No inactive instructors should return an empty list");
    }

    /**
     * Corner case: no instructors in the system at all.
     * F4 should return an empty list - not null.
     */
    @Test
    void testF4_GetInstructorsWithNoStudents_NoInstructors_ReturnsEmptyList() {
        when(instructorRepository.findAll()).thenReturn(Collections.emptyList());
        when(enrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        List<Instructor> result = lmsService.getInstructorsWithNoStudents();

        assertNotNull(result, "Result should never be null");
        assertTrue(result.isEmpty(), "No instructors in system should return empty list");
    }
}
