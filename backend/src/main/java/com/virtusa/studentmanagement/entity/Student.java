package com.virtusa.studentmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Student Entity mapped to the MySQL 'students' table.
 * 
 * Member 3 Deliverable: Entity definition and JPA mapping.
 * 
 * Fields:
 * - id: Auto-generated primary key (Student ID)
 * - name: Full name of the student
 * - email: Unique student email address
 * - department: Department/Major (e.g., Computer Science, IT)
 * - yearOfStudy: Current year of study (1 to 4)
 * - phoneNumber: 10-digit contact number
 */
@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
    }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student name is required")
    @Size(min = 2, max = 100, message = "Student name must be between 2 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Student email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @NotNull(message = "Year of study is required")
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 4, message = "Year of study cannot exceed 4")
    @Column(name = "year_of_study", nullable = false)
    private Integer yearOfStudy;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    /**
     * Default no-argument constructor (required by JPA / Hibernate).
     */
    public Student() {
    }

    /**
     * Parameterized constructor without ID (used for creating new records before persistence).
     */
    public Student(String name, String email, String department, Integer yearOfStudy, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.phoneNumber = phoneNumber;
    }

    /**
     * All-arguments constructor.
     */
    public Student(Long id, String name, String email, String department, Integer yearOfStudy, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.phoneNumber = phoneNumber;
    }

    // ===================================================================
    // Getters and Setters
    // ===================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ===================================================================
    // equals, hashCode and toString
    // ===================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        if (id != null && student.id != null) {
            return Objects.equals(id, student.id);
        }
        return Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", yearOfStudy=" + yearOfStudy +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
