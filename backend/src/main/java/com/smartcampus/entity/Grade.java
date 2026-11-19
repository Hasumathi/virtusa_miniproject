package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_subject_grade", columnNames = {"student_id", "subject_id", "semester", "academic_year"})
}, indexes = {
        @Index(name = "idx_grade_student", columnList = "student_id"),
        @Index(name = "idx_grade_subject_sem", columnList = "subject_id, semester")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    private Double internalMarks;
    private Double assignmentMarks;
    private Double practicalMarks;
    private Double examMarks;

    @Column(nullable = false)
    private Double totalMarks;

    @Column(nullable = false)
    private Double percentage;

    @Column(nullable = false, length = 5)
    private String letterGrade;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPassed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
