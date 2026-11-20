package com.smartcampus.repository;

import com.smartcampus.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByStudentIdAndIsPublishedTrue(Long studentId);

    List<Grade> findByStudentIdAndSemesterAndIsPublishedTrue(Long studentId, Integer semester);

    List<Grade> findBySubjectIdAndSemester(Long subjectId, Integer semester);

    Optional<Grade> findByStudentIdAndSubjectIdAndSemesterAndAcademicYear(
            Long studentId, Long subjectId, Integer semester, String academicYear);

    boolean existsByStudentIdAndSubjectIdAndSemesterAndAcademicYear(
            Long studentId, Long subjectId, Integer semester, String academicYear);

    @Query("SELECT AVG(g.percentage) FROM Grade g WHERE g.isPublished = true")
    Double findOverallAveragePercentage();

    @Query("SELECT AVG(g.percentage) FROM Grade g WHERE g.student.id = :studentId AND g.isPublished = true")
    Double findAveragePercentageByStudentId(@Param("studentId") Long studentId);
}
