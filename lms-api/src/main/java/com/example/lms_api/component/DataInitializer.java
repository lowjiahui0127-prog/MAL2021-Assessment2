package com.example.lms_api.component;

import com.example.lms_api.model.*;
import com.example.lms_api.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    @Autowired private StudentRepository studentRepo;
    @Autowired private InstructorRepository instructorRepo;
    @Autowired private CourseRepository courseRepo;
    @Autowired private EnrollmentRepository enrollmentRepo;

    @Override
    public void run(String... args) throws Exception {
        // Instructors
        Instructor profGrace = instructorRepo.save(new Instructor("Ms. Grace", "grace@peninsulamalaysia.edu.my"));
        Instructor profEric = instructorRepo.save(new Instructor("Mr. Eric", "eric@peninsulamalaysia.edu.my"));
        Instructor profAmir = instructorRepo.save(new Instructor("Mr. Amir",   "amir@peninsulamalaysia.edu.my")); // no courses at all

        // Students
        Student studentJiaHui = studentRepo.save(new Student("JiaHui", "bsse2509252@peninsulamalaysia.edu.my"));
        Student studentCeline = studentRepo.save(new Student("Celine", "bsse2509249@peninsulamalaysia.edu.my"));
        Student studentSulyn = studentRepo.save(new Student("Sulyn", "bsse2509244@peninsulamalaysia.edu.my")); // not enrolled

        // Course
        Course courseMAL2021 = courseRepo.save(new Course("MAL2021 Software Development Tools and Practices", profGrace));
        Course courseMAL2020 = courseRepo.save(new Course("MAL2020 Computing Group Project", profGrace));
        Course courseMAL2019 = courseRepo.save(new Course("MAL2019 Artificial Intelligence", profEric)); // Mr. Eric has a course but NO enrollments

        // Enrollments
        enrollmentRepo.save(new Enrollment(studentJiaHui, courseMAL2021));
        enrollmentRepo.save(new Enrollment(studentJiaHui, courseMAL2020));
        enrollmentRepo.save(new Enrollment(studentCeline, courseMAL2021));

        System.out.println("LMS Sample Data Initialized successfully!");
    }
}
