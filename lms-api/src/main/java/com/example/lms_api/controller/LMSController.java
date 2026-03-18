package com.example.lms_api.controller;

import com.example.lms_api.model.*;
import com.example.lms_api.service.LMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lms") // Base path for all LMS functions
public class LMSController {

    @Autowired
    private LMSService lmsService;

    // F1: Endpoint to retrieve all course enrollments for a specific student.
    @GetMapping("/students/{id}/enrollments")
    public List<Enrollment> getStudentEnrollments(@PathVariable Long id) {
        return lmsService.getStudentEnrollments(id);
    }

    // F2: Endpoint to return a list of students currently enrolled in courses.
    @GetMapping("/students/active")
    public List<Student> getActiveStudents() {
        return lmsService.getActiveStudents();
    }

    // F3: Endpoint to determine which instructor has the highest number of student enrollments.
    @GetMapping("/instructors/most-active")
    public Instructor getMostActiveInstructor() {
        return lmsService.getMostActiveInstructor();
    }

    // F4: Endpoint to return a list of instructors who currently have zero students.
    @GetMapping("/instructors/no-students")
    public List<Instructor> getInstructorsWithNoStudents() {
        return lmsService.getInstructorsWithNoStudents();
    }
}
