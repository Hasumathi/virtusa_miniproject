package com.smartcampus.dto.response;

import com.smartcampus.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private String studentRegisterNumber;
    private String studentName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private String remarks;
    private Long markedByFacultyId;
    private String markedByFacultyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
