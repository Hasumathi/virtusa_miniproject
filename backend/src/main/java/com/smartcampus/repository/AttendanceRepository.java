package com.smartcampus.repository;

import com.smartcampus.entity.Attendance;
import com.smartcampus.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySubjectIdAndAttendanceDate(Long subjectId, LocalDate attendanceDate);

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    Page<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId, Pageable pageable);

    Optional<Attendance> findByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate attendanceDate);

    boolean existsByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate attendanceDate);

    long countByStudentIdAndSubjectId(Long studentId, Long subjectId);

    long countByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, AttendanceStatus status);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = :status")
    long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceStatus status);
}
