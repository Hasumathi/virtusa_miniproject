package com.smartcampus.repository;

import com.smartcampus.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByIdAndIsDeletedFalse(Long id);

    Optional<Student> findByRegisterNumberAndIsDeletedFalse(String registerNumber);

    Optional<Student> findByEmailAndIsDeletedFalse(String email);

    Optional<Student> findByUserId(Long userId);

    boolean existsByRegisterNumber(String registerNumber);

    boolean existsByRegisterNumberAndIdNot(String registerNumber, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByIsDeletedFalse();

    long countByDepartmentIdAndIsDeletedFalse(Long departmentId);
}
