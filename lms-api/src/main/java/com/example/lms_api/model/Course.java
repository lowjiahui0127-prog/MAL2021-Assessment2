package com.example.lms_api.model;

import jakarta.persistence.*;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseName;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    // Constructors
    public Course() {}
    public Course(String courseName, Instructor instructor) {
        this.courseName = courseName;
        this.instructor = instructor;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }
}
