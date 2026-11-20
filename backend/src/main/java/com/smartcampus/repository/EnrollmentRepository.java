package com.smartcampus.repository;

import com.smartcampus.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findBySubjectId(Long subjectId);

    List<Enrollment> findBySubjectIdAndAcademicYearAndSemester(
            Long subjectId, String academicYear, Integer semester);

    boolean existsByStudentIdAndSubjectIdAndAcademicYearAndSemester(
            Long studentId, Long subjectId, String academicYear, Integer semester);

    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.semester = :semester")
    List<Enrollment> findByStudentIdAndSemester(
            @Param("studentId") Long studentId,
            @Param("semester") Integer semester);

    long countBySubjectId(Long subjectId);
    
    long countByStudentId(Long studentId);
}
