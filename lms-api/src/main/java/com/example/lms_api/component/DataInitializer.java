package com.example.lms_api.component;

import com.example.lms_api.model.*;
import com.example.lms_api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private StudentRepository studentRepo;
    @Autowired private InstructorRepository instructorRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    @Override
    public void run(String... args) throws Exception {
        // 1. Create Instructors
        Instructor profGrace = instructorRepo.save(new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my"));
        Instructor profEric = instructorRepo.save(new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my")); // No students

        // 2. Create Students
        Student studentJiaHui = studentRepo.save(new Student("JiaHui", "bsse2509252@peninsulamalaysia.edu.my"));
        Student studentCeline = studentRepo.save(new Student("Celine", "bsse2509249@peninsulamalaysia.edu.my"));

        // 3. Create Course
        Course javaCourse = courseRepo.save(new Course("MAL2021 Software Development Tools and Practices", profGrace));

        // 4. Create Enrollments (Only JiaHui is enrolled)
        enrollmentRepo.save(new Enrollment(studentJiaHui, javaCourse));

        System.out.println("LMS Sample Data Initialized successfully!");
    }
}
