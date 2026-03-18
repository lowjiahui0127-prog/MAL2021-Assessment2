package com.example.lms_api.repository;

import com.example.lms_api.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}
