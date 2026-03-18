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

    @Test
    void testF1_GetStudentEnrollments() {
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

    @Test
    void testF2_GetActiveStudents() {
        // Arrange: Create duplicate student enrollments to test distinct logic
        Student student = new Student("JiaHui", "bsse2509252@peninsulamalaysia.edu.my");
        Enrollment e1 = new Enrollment(); e1.setStudent(student);
        Enrollment e2 = new Enrollment(); e2.setStudent(student);
        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        // Act: Execute the method
        List<Student> activeStudents = lmsService.getActiveStudents();

        // Assert: Check if only 1 unique student is returned
        assertEquals(1, activeStudents.size());
        assertEquals("JiaHui", activeStudents.get(0).getName());
    }

    @Test
    void testF3_GetMostActiveInstructor() {
        // Arrange: Setup Instructors and Enrollments
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        Instructor profEric = new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my");

        Course c1 = new Course("MAL2021 Software Development Tools and Practices", profGrace);
        Course c2 = new Course("MAL2020 Computing Group Project", profEric);

        Enrollment e1 = new Enrollment(); e1.setCourse(c1); // Grace +1
        Enrollment e2 = new Enrollment(); e2.setCourse(c1); // Grace +2
        Enrollment e3 = new Enrollment(); e3.setCourse(c2); // Eric +1

        when(enrollmentRepository.findAll()).thenReturn(Arrays.asList(e1, e2, e3));

        // Act
        Instructor mostActive = lmsService.getMostActiveInstructor();

        // Assert
        assertNotNull(mostActive);
        assertEquals("Ms. Grace", mostActive.getName());
    }

    @Test
    void testF4_GetInstructorsWithNoStudents() {
        // Arrange: Setup one instructor with students and one without
        Instructor profGrace = new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my");
        profGrace.setId(1L);
        Instructor profEric = new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my");
        profEric.setId(2L);

        Course c1 = new Course("MAL2021 Software Development Tools and Practices", profGrace);
        Enrollment e1 = new Enrollment(); e1.setCourse(c1);

        when(instructorRepository.findAll()).thenReturn(Arrays.asList(profGrace, profEric));
        when(enrollmentRepository.findAll()).thenReturn(Collections.singletonList(e1));

        // Act
        List<Instructor> result = lmsService.getInstructorsWithNoStudents();

        // Assert: Only Eric has no students
        assertEquals(1, result.size());
        assertEquals("Mr. Eric", result.get(0).getName());
    }
}
