package com.example.lms_api.repository;

import com.example.lms_api.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
