package com.smartcampus.repository;

import com.smartcampus.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmployeeId(String employeeId);
    Optional<Faculty> findByEmail(String email);
    Optional<Faculty> findByUserId(Long userId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmail(String email);
    List<Faculty> findByDepartmentId(Long departmentId);
}
