package com.smartcampus.repository;

import com.smartcampus.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findBySubjectCode(String subjectCode);
    boolean existsBySubjectCode(String subjectCode);
    boolean existsBySubjectCodeAndIdNot(String subjectCode, Long id);
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByDepartmentIdAndSemester(Long departmentId, Integer semester);
    Page<Subject> findByDepartmentId(Long departmentId, Pageable pageable);
}
